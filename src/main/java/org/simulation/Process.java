package org.simulation;

public class Process extends Thread{
    private String _nameProgrammer; 
    private char _operation;
    private int _time;

    private String _status;

    public String getStatus() {
        return _status;
    }

    public void setStatus(String _status) {
        this._status = _status;
    }

    public long getTimeMilli() {
        return _timeMilli;
    }

    private long _timeMilli;

    private int _id;

    private int _leftNumber;
    private int _rightNumber;

    private long _start;
    private long _end;

    private double _result;

    public double getResult() {
        return _result;
    }

    public void setNameProgrammer(String _nameProgrammer) {
        this._nameProgrammer = _nameProgrammer;
    }
    public void setOperation(char _operation) {
        this._operation = _operation;
    }
    public void setTime(int _time) {
        this._time = _time;
    }

    public String getNameProgrammer() {
        return _nameProgrammer;
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

    public double getStart(){
        return millisecondsToSeconds(this._start);
    }

    public double getEnd(){
        return millisecondsToSeconds(this._end);
    }

    public static double millisecondsToSeconds(long milli){
        return ((double)milli/1000.0);
    }

    Process(String name,int id, int time, char operation,int leftNumber,int rightNumber,String status){
        this._nameProgrammer = name;
        this._id = id;
        this._time = time;
        this._timeMilli = time * 1000;

        this._operation = operation;
        this._leftNumber = leftNumber;
        this._rightNumber = rightNumber;
        this._result = 0;

        this._status = status;
    }

    public synchronized long getRunTime(){
        if(this._start == 0) return 0;

        long runTime = System.currentTimeMillis() - this._start;

        return runTime > _timeMilli? _timeMilli : runTime;
    }

    public synchronized double getProgress(){
        return getRunTime()/(double)this._timeMilli;
    }

    public synchronized long getRemainingTIme(){
        return this._timeMilli - getRunTime();
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
        System.out.println(_result);
        return String.format("%1d %2c %3d = %4$.2f",
            _leftNumber,_operation,_rightNumber,_result 
        );
    }

    @Override
    public void run(){
        synchronized(this){
            this._start = System.currentTimeMillis();
        }

        try{
            synchronized(this){
                this.wait(this._timeMilli);
            }
        }catch(InterruptedException ex){

        }

        synchronized(this){
            this._end = System.currentTimeMillis(); 
            this._result = generateResult();
        }
    }

}
