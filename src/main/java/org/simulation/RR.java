package org.simulation;

import javafx.collections.ObservableList;

public class RR extends Thread{
    private final int max_size = 3;
    private ObservableList<Process> _process;
    private boolean _isEnd;
    private Process _current;
    private Manager _manager;
    private boolean _wait;
    private long _totalRunTime;
    private Boolean _nullProcess;
    private Chronotime _chronotime;
    private long _quantum;
    private long _endQuantum;

    public RR(Manager manager){
        _isEnd = false;
        _nullProcess = false;
        _manager = manager;
        _chronotime = new Chronotime(manager,9000);
        _chronotime.start();
        _quantum = 1000;
    }
    public void setEnd(){
        _isEnd = true;
    }

    public void setReadyList(ObservableList<Process> readylist){
        _process = readylist;
    }
    public synchronized long getCurrentProcessRunTime(){
        if(_current == null) return 0;

        return _current.getRunTime();
    }

    public synchronized double getCurrentProcessProgress(){
        if(_current == null) return 0;

        return _current.getProgress();
    }

    public synchronized long getCurrentProcessRemainingTime(){
        if(_current == null) return 0;

        return _current.getRemainingTIme();
    }

    public synchronized void setInterruption(){
        if(_current == null) return;
        synchronized (RR.class){
            Process process = _process.remove(0);
            process.setIndex(_chronotime.add());
            _manager.addWaiting(process);
        }

        _current.startWait();
    }

    public synchronized void setError(){
        _current.setError();
    }

    public synchronized void startWait(){
        System.out.println("Message RR to sleep");
        setWait(true);

        _current.startWait();
        _chronotime.startWaiting();
    }
    public synchronized void setWait(boolean value){
        _wait = value;
    }
    public synchronized boolean getWait(){
        return _wait;
    }

    public synchronized void setContinue(){
        if(getWait() && getState() == State.WAITING){
            synchronized (this){
                this.notify();
            }
        }
    }
    public long getGlobalTime(){
        return _manager.getGlobalTime();
    }
    public Long getTotalRunTime(){
        return _totalRunTime;
    }

    public void setQuantum(int quantum){
        _quantum = quantum * 1000L;
    }

    @Override
    public void run(){
        while(!_manager.isEnd()){
            if(_nullProcess){
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                _nullProcess = false;
            }

            synchronized(this){
                if(_process.isEmpty()){
                    _current = null;
                    _nullProcess = true;
                    continue;
                }

                _current = _process.get(0);
                _current.setChronotime(_chronotime);

                _current.setStatus("Ejecutandose...");
                _manager.receiveProcessInfo(_current);
                _manager.sendUpdateInfoList();

                if(_current.getState() == State.WAITING){
                    _current.awake();
                }else{
                    _totalRunTime += _current.getTimeMilli();
                    try{
                        _current.start();
                    }catch (IllegalThreadStateException ex){
                        System.out.println(ex.toString());
                    }
                }

                _endQuantum = _manager.getGlobalTime() + _quantum;
                System.out.println(_endQuantum);
            }

            boolean flagQuantum = false;
            for(;;){
                if(!getWait() && (_current.isWaiting() || !_current.isAlive())){
                    break;
                }

                if(getWait()){
                    _manager.sendUpdateInfoList();

                    try {
                        synchronized (this){
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Espera en Batch");
                    }

                    _current.awake();

                    _chronotime.awake();
                    setWait(false);
                    continue;
                }

                if(_manager.getGlobalTime() >= _endQuantum){
                    if(!isAlive()){
                        break;
                    }

                    if(_current.isCloseToEnd()){
                        try {
                            _current.join();
                            break;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    _current.startWait();
                    synchronized (RR.class){
                        _process.remove(0);
                        _process.add(_current);
                    }

                    _current.setStatus("Listo");

                    flagQuantum = true;
                    break;
                }


                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

            if(flagQuantum) continue;

            if(!_current.isWaiting() || !_current.isAlive()){
                Result aux = new Result();
                aux.setId(_current.getID());
                aux.setResultado(_current.getResultString());
                aux.setProcessInfo(_current);

                _current.setStatus("Terminado");
                _manager.sendUpdateInfoList();
                _manager.receiveResult(aux);

                _manager.removeMemoryProcess();
            }else{
                _current.setStatus("Bloqueado");
                _manager.sendUpdateInfoList();
            }

            if(_current.isError()){
                _totalRunTime -= _current.getRemainingTIme();
                _current.setStatus("Terminado");
                _manager.sendUpdateInfoList();
            }
        }
        _chronotime.endThread();
    }

}
