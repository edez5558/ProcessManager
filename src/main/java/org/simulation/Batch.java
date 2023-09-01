package org.simulation;

public class Batch extends Thread{
    private Process[] _process;
    private Process _currentProcess;
    private int _id;
    private Manager _manager;
    private int _totalRunTime;

    public int getTotalRunTime() {
        return _totalRunTime;
    }

    @Override
    public void run(){
        for(int i = 0; i < _process.length; i++){
            synchronized(this){
                _currentProcess = _process[i];
                _manager.receiveProcessInfo(_currentProcess);
                _currentProcess.setStatus("Ejecutandose...");
                _totalRunTime += _currentProcess.getTimeMilli();
                _manager.sendUpdateInfoList();
                _currentProcess.start();
            }

            try {
                _currentProcess.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Result aux = new Result();
            aux.setId(_currentProcess.getID());
            aux.setResultado(_currentProcess.getResultString());
            aux.setLote(_id);

            _currentProcess.setStatus("Terminado");
            _manager.sendUpdateInfoList();
            _manager.receiveResult(aux);
        }
    }

    public synchronized long getCurrentProcessRunTime(){
        if(_currentProcess == null) return 0;
        
        return _currentProcess.getRunTime();
    }

    public synchronized double getCurrentProcessProgress(){
        if(_currentProcess == null) return 0;

        return _currentProcess.getProgress();
    }

    public synchronized long getCurrentProcessRemainingTime(){
        if(_currentProcess == null) return 0;

        return _currentProcess.getRemainingTIme();
    }

    Batch(int max,int id,Manager manager){
        _process = new Process[max];
        _manager = manager;
        _totalRunTime = 0;
        _id = id;
    } 

    public void setProcessAt(int index,Process process){
        _process[index] = process;
    }


    public boolean isEmptyAt(int index){
        return _process[index] == null;
    }

    public Process getIndex(int index){
        if(index >= 0 && index < _process.length)
            return _process[index];
        else
            return null;
    }

    public int getMaxIndex(){
        return _process.length;
    }

    public boolean avaliableID(int ID){
        for(int i = 0; i < _process.length; i++){
            if(_process[i] != null && _process[i].getID() == ID)
                return false;
        }

        return true;
    }
}
