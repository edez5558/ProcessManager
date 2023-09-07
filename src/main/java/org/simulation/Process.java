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
    private boolean _stop;
    private long _expectedEnd;

    private double _result;

    private Batch _batch;
    private Boolean _wait = false;


    public double getResult() {
        return _result;
    }

    public void setOperation(char _operation) {
        this._operation = _operation;
    }
    public void setTime(int _time) {
        this._time = _time;
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

    public void setLeftNumber(int _leftNumber) {
        this._leftNumber = _leftNumber;
    }

    public int getRightNumber() {
        return _rightNumber;
    }

    public void setRightNumber(int _rightNumber) {
        this._rightNumber = _rightNumber;
    }



    public static double millisecondsToSeconds(long milli){
        return ((double)milli/1000.0);
    }

    Process(int time, char operation,int leftNumber,int rightNumber,String status){
        this._id = id++;
        this._time = time;
        this._timeMilli = time * 1000;

        this._operation = operation;
        this._leftNumber = leftNumber;
        this._rightNumber = rightNumber;
        this._result = 0;
        this._sumTime = 0;
        this._expectedEnd = 0;

        this._status = status;
    }

    public synchronized long getRunTime(){
        if(this._start == 0) return 0;
        if(getState() == State.WAITING) return this._sumTime;

        long runTime = this._sumTime + System.currentTimeMillis() - this._start;

        return runTime > _timeMilli? _timeMilli : runTime;
    }

    public synchronized double getProgress(){
        return getRunTime()/(double)this._timeMilli;
    }

    public synchronized long getRemainingTIme(){
        if(_stop) return this._timeMilli - this._sumTime;
        return this._timeMilli - getRunTime();
    }

    public void startWait(){
        this._wait = true;
    }

    public synchronized void awake(){
        this._start = System.currentTimeMillis();
        this._expectedEnd += System.currentTimeMillis();
        this._wait = false;

        synchronized (this){
            this.notify();
        }
    }

    public synchronized void setError(){
        this._stop = true;
    }

    private double generateResult(){
        switch(this._operation){
            case '+': return _leftNumber + _rightNumber;
            case '-': return _leftNumber - _rightNumber;
            case '*': return _leftNumber * _rightNumber;
            case '/': return (double)_leftNumber / _rightNumber;
            case '^': return Math.pow(_leftNumber,_rightNumber);
            case '%': return _leftNumber % _rightNumber;
        }

        return 0;
    }

    public String getResultString(){
        if(_stop) return "ERROR";

        return String.format("%1d %2c %3d = %4$.2f",
            _leftNumber,_operation,_rightNumber,_result 
        );
    }

    @Override
    public void run(){
        synchronized(this){
            this._expectedEnd = System.currentTimeMillis() + _timeMilli;
            this._start = System.currentTimeMillis();
        }

        //start  + _timeMilli == endTime;
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

            if(this._wait){
                synchronized (this){
                    synchronized (this._batch){
                        this._batch.notify();
                    }

                    this._sumTime += System.currentTimeMillis() - this._start;
                    this._expectedEnd -= System.currentTimeMillis();

                    try {
                        this.wait();
                    } catch (InterruptedException e) {

                    }

                }
            }
        }

        synchronized(this){
            this._result = generateResult();
        }

        synchronized (this._batch){
            this._batch.notify();
        }
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

    public boolean isWaiting(){
        return _wait;
    }
    public void setBatch(Batch _batch) {
        this._batch = _batch;
    }
}