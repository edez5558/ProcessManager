package org.simulation;

public class Process extends Thread{
    private char _operation;
    private int _time;
    private static int id;

    private String _status;

    private long _timeMilli;

    private int _id;

    private int _leftNumber;
    private int _rightNumber;

    private long _start;
    private long _sumTime;
    private long _sumWait;
    private long _startWaiting;
    private boolean _stop;
    private long _expectedEnd;

    private double _result;

    private RR _RR;
    private Boolean _wait = false;

    private int index;

    private long _quantum;

    public synchronized int getIndex() {
        return index;
    }

    public synchronized void setIndex(int index) {
        this.index = index;
    }

    public double getResult() {
        return _result;
    }
    public char getOperation() {
        return _operation;
    }
    public int getTime() {
        return _time;
    }
    public int getID() {
        return _id;
    }

    public int getLeftNumber() {
        return _leftNumber;
    }
    public double getWaitingTime(){
        if(_startWaiting == 0)  return 0;

        return millisecondsToSeconds(_chronotime.getProcessTime(this));
    }
    public int getRightNumber() {
        return _rightNumber;
    }

    public static double millisecondsToSeconds(long milli){
        return ((double)milli/1000.0);
    }

    private long _timeEnter;
    private long _timeStart;
    private long _timeStop;
    private Chronotime _chronotime;
    public void setChronotime(Chronotime chronotime){
        _chronotime = chronotime;
    }
    Process(int time, char operation, int leftNumber, int rightNumber, String status, RR RR){
        this._id = id++;
        this._time = time;
        this._timeMilli = time * 1000;

        this._operation = operation;
        this._leftNumber = leftNumber;
        this._rightNumber = rightNumber;
        this._result = 0;
        this._sumTime = 0;
        this._expectedEnd = 0;

        this._RR = RR;

        this._status = status;
        this._timeStop = -1;
        this._timeStart = -1;
        this._timeEnter = -1;
        setIndex(-1);

        _textResult = String.format("%1d %2c %3d",
                _leftNumber,_operation,_rightNumber
        );
    }

    public synchronized long getRunTime(){
        if(this._start == 0) return 0;
        if(getState() == State.WAITING) return this._sumTime;

        long runTime = this._sumTime + System.currentTimeMillis() - this._start;

        return runTime > _timeMilli? _timeMilli : runTime;
    }

    public boolean isCloseToEnd(){
        return getRemainingTIme() < 50;
    }

    public synchronized double getProgress(){
        return getRunTime()/(double)this._timeMilli;
    }

    public synchronized long getRemainingTIme(){
        if(_stop) return this._timeMilli - this._sumTime;
        return this._timeMilli - getRunTime();
    }

    public synchronized void startWait(){
        setWait(true);
        this._startWaiting = System.currentTimeMillis();
    }

    public synchronized void awake(){
        this._start = System.currentTimeMillis();
        this._expectedEnd += System.currentTimeMillis();
        setWait(false);
        this._startWaiting = 0;

        synchronized (this){
            this.notify();
        }
    }

    public synchronized void setError(){
        this._stop = true;
    }

    private double generateResult(){
        return switch (this._operation) {
            case '+' -> _leftNumber + _rightNumber;
            case '-' -> _leftNumber - _rightNumber;
            case '*' -> _leftNumber * _rightNumber;
            case '/' -> (double) _leftNumber / _rightNumber;
            case '^' -> Math.pow(_leftNumber, _rightNumber);
            case '%' -> _leftNumber % _rightNumber;
            default -> 0;
        };
    }

    public String getResultString(){
        if(_stop){
            _textResult += " = ERROR";
            return "ERROR";
        }

        _textResult += String.format(" = %1$.2f",_result);

        return String.format("%1d %2c %3d = %4$.2f",
            _leftNumber,_operation,_rightNumber,_result 
        );

    }
    public void setEnterTime(){
        _timeEnter = _RR.getGlobalTime();
    }

    @Override
    public void run(){
        synchronized(this){
            this._expectedEnd = System.currentTimeMillis() + _timeMilli;
            this._start = System.currentTimeMillis();
            this._timeStart = _RR.getGlobalTime();
        }

        for(;;){
            synchronized (this){
                if(System.currentTimeMillis() >= _expectedEnd){
                    break;
                }
            }

            if(this._stop){
                this._sumTime += System.currentTimeMillis() - this._start;
                break;
            }

            if(getWait()){
                synchronized (this){
                    this._sumTime += System.currentTimeMillis() - this._start;
                    this._expectedEnd -= System.currentTimeMillis();

                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }

                    setIndex(-1);
                }
            }
        }

        synchronized(this){
            this._result = generateResult();
        }

        synchronized (this._RR){
            this._timeStop = _RR.getGlobalTime();
            this._RR.notify();
        }
    }


    public synchronized boolean getWait(){
        return _wait;
    }
    public synchronized void setWait(boolean value){
        _wait = value;
    }
    public Double getTimeEnter(){
        if(_timeEnter < 0) return null;

        return Process.millisecondsToSeconds(_timeEnter);
    }
    public Double getTimeStop(){
        if(_timeEnter < 0)  return null;
        return Process.millisecondsToSeconds(_timeStop);
    }

    public long getTimeStopMilli(){
        return _timeStop;
    }

    public Double getTimeReturn(){
        if(_timeEnter < 0) return null;

        if(_timeStop < 0) return -1.0;

        return getTimeStop() - getTimeEnter();
    }

    public Double getTimeRespond(){
        if(_timeEnter < 0) return null;

        if(_timeStart < 0) return -1.0;

        return Process.millisecondsToSeconds(_timeStart - _timeEnter);
    }

    public synchronized Double getTimeWaiting(){
        if(_timeEnter < 0) return null;

        if(_timeStop > 0)
            return getTimeReturn() - getTimeService();

        return Process.millisecondsToSeconds(_RR.getGlobalTime() - _timeEnter) - getTimeService();
    }
    public synchronized Double getTimeService(){
        if(_timeEnter < 0) return null;
        if(this._timeStop < 0){
            return Process.millisecondsToSeconds(getRunTime());
        }

        if(isError()) return Process.millisecondsToSeconds(getTimeMilli() - getRemainingTIme());

        return Process.millisecondsToSeconds(getTimeMilli());
    }

    public Double getTimeRemaining(){
        if(_timeEnter < 0)  return null;
        return Process.millisecondsToSeconds(getRemainingTIme());
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String _status) {
        this._status = _status;
    }

    public long getTimeMilli() {
        return _timeMilli;
    }
    public boolean isError(){
        return _stop;
    }

    public synchronized boolean isWaiting(){
        return _wait;
    }
    public void setFCFS(RR RR) {
        this._RR = RR;
    }

    private String _textResult;
    public String getTextResult(){
        return _textResult;
    }
    public void setTextResult(String text){
        _textResult = text;
    }
}