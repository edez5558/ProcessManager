package org.simulation;

import java.util.LinkedList;
import java.util.Queue;

public class Batch extends Thread{
    private Process[] _process;
    private Process _currentProcess;
    private Queue<Process> _queue;
    private int _id;
    private Manager _manager;
    private int _totalRunTime;
    private boolean _wait = false;

    public int getTotalRunTime() {
        return _totalRunTime;
    }
    public synchronized void setInterruption(){
        _currentProcess.startWait();
        _queue.add(_currentProcess);
    }

    public synchronized void setError(){
        _currentProcess.setError();
    }
    public synchronized void setWait(){
        _wait = true;
        _currentProcess.startWait();
    }

    public synchronized void setContinue(){
        if(_wait && getState() == State.WAITING){
            synchronized (this){
                this.notify();
            }
        }
    }


    @Override
    public void run(){
        for(Process process = _queue.remove(); process != null; ){
            synchronized(this){
                _currentProcess = process;
                _currentProcess.setBatch(this);
                _currentProcess.setStatus("Ejecutandose");
                _manager.receiveProcessInfo(_currentProcess);
                _manager.sendUpdateInfoList();

                if(_currentProcess.getState() == State.WAITING) {
                    _currentProcess.awake();
                }else{
                    _totalRunTime += _currentProcess.getTimeMilli();
                    _currentProcess.start();
                }

            }

            try {
                synchronized (this){
                    this.wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupcion en Batch");
            }


            if(_wait){

                _manager.sendUpdateInfoList();
                try {
                    synchronized (this){
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Espera en Batch");
                }

                _wait = false;
                continue;
            }

            if(!_currentProcess.isWaiting()){
                Result aux = new Result();
                aux.setId(_currentProcess.getID());
                aux.setResultado(_currentProcess.getResultString());
                aux.setLote(_id);

                _currentProcess.setStatus("Terminado");
                _manager.sendUpdateInfoList();
                _manager.receiveResult(aux);
            }else{
                _currentProcess.setStatus("Esperando...");
                _manager.sendUpdateInfoList();
            }

            if(_currentProcess.isError()){
                _totalRunTime -= _currentProcess.getRemainingTIme();
                _currentProcess.setStatus("ERROR");
                _manager.sendUpdateInfoList();
            }

            if(!_queue.isEmpty())
                process = _queue.remove();
            else
                process = null;
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
        _queue = new LinkedList<Process>();
        _manager = manager;
        _totalRunTime = 0;
        _id = id;
    } 

    public void setProcessAt(int index,Process process){
        _process[index] = process;
        setQueueProcess(process);
    }

    public void setQueueProcess(Process process){
        _queue.add(process);
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
