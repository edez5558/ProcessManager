package org.simulation;

import javafx.collections.ObservableList;

import java.util.*;

public class Manager extends Thread{
    private final static Random random = new Random();
    private long _start;
    private long _sumTime = 0;
    private boolean _stopGlobal = false;

    private final Controller _controller;

    private ObservableList<Process> _news;
    private ObservableList<Process> _ready;
    private ObservableList<Process> _waiting;
    private ObservableList<Process> _all;
    private final RR _RR;
    private Process _current;
    private int _memoryprocess = 0;
    public boolean isFull(){
        return _memoryprocess >= 3;
    }
    public void addMemoryProcess(Process process){
        process.setEnterTime();

        synchronized (RR.class){
            _memoryprocess++;
            _ready.add(process);
        }
    }
    public Process removeMemoryProcess(){
        if(_memoryprocess <= 0) return null;

        Process aux = null;
        synchronized (RR.class){
            aux = _ready.remove(0);
            _memoryprocess--;
        }

        return aux;
    }

    public Process randomProcess(){
        Process aux = new Process(
                random.nextInt(18-7) + 7,
                randomOperation(),
                random.nextInt(20) + 1,
                random.nextInt(20) + 1,
                "Nuevo",
                _RR);

        _all.add(aux);
        return aux;
    }
    public void newProcess(){
        Process aux = randomProcess();

        if(isFull())
            _news.add(aux);
        else{
            addMemoryProcess(aux);
        }
    }
    public void fillProcess(int n_process, ObservableList<Process> process,
                            ObservableList<Process> ready,ObservableList<Process> waiting,
                            ObservableList<Process> all){
        _news = process;
        _ready = ready;
        _waiting = waiting;
        _all = all;

        for(int i = 0; i < n_process; i++){
            process.add(randomProcess());
        }
    }
    Manager(Controller controller){
        _controller = controller;
        _RR = new RR(this);
    }

    public void setQuantum(int quantum){
        _RR.setQuantum(quantum);
    }

    public void changeToReady(){
        Process process = _waiting.remove(0);

        System.out.println(_ready.size());
        if(process != null){
            process.setStatus("Listo");
            _ready.add(process);
            System.out.println("Un proceso en espera a sido colocado en ready");
        }
        else
            System.out.println("What??");
    }
    public void addWaiting(Process process){
        synchronized (RR.class){
            _waiting.add(process);
        }
    }
    public void paintBatch(){
        while(true){
            synchronized(RR.class){
                if(_current.getState() == State.TERMINATED ||
                   _current.getState() == State.WAITING)
                    break;
            }

            _controller.updateGlobalTime(getGlobalTime());
            _controller.updateProcessTime(_RR.getCurrentProcessProgress());
            _controller.updateTimeRemaining(_RR.getCurrentProcessRemainingTime());
            _controller.updateRunTime(_RR.getCurrentProcessRunTime());
            _controller.updateRemainingProcess();
            _controller.refreshTableBlock();
            _controller.refreshTableTime();
            sendUpdateInfoList();

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void paintWithoutProcess(){
        while(true){
            synchronized(RR.class){
                if(!_ready.isEmpty() || isEnd())
                    break;
            }

            _controller.updateGlobalTime(getGlobalTime());
            _controller.refreshTableBlock();
            sendUpdateInfoList();

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isAllEmpty(){
        return _news.isEmpty() && _ready.isEmpty() && _waiting.isEmpty();
    }
    public boolean isEnd(){
        return _memoryprocess <= 0;
    }

    @Override
    public void run(){
        startGlobalTime();

        _current = _news.remove(0);
        _current.setStatus("Listo");

        addMemoryProcess(_current);

        for(int i = 1; i < 3; i++){
            if(!_news.isEmpty()){
                Process aux = _news.remove(0);
                aux.setStatus("Listo");

                addMemoryProcess(aux);
            }
        }


        _RR.setReadyList(_ready);
        _RR.start();

        while(!isEnd()){
            paintBatch();

            if(_ready.isEmpty())
                paintWithoutProcess();

            if(!_ready.isEmpty() && _ready.get(0) != _current)
                _current = _ready.get(0);

            synchronized (RR.class){
                if(!isFull()){
                    if(isAllEmpty()) {
                        _RR.setEnd();
                        break;
                    }
                    synchronized (RR.class){
                        if(_news.isEmpty()) continue;

                        Process aux = _news.remove(0);
                        aux.setStatus("Listo");

                        addMemoryProcess(aux);
                    }
                }
            }

        }

        sendEndMessage();
    }

    public synchronized void sendInterruption(){
        if(_RR != null)
            _RR.setInterruption();
    }

    public synchronized void sendError(){
        if(_RR != null)
            _RR.setError();
    }
    public synchronized void setStopGlobal(boolean value){
        _stopGlobal = value ;
    }
    public synchronized boolean getStopGlobal(){
        return _stopGlobal;
    }

    public void sendPause(){
        if(_RR != null && !getStopGlobal()){
            _sumTime += System.currentTimeMillis() - _start;
            _RR.startWait();
            setStopGlobal(true);
        }
    }

    public void sendContinue(){
        if(_RR != null && getStopGlobal()){
            _start = System.currentTimeMillis();
            _RR.setContinue();
            setStopGlobal(false);
        }
    }


    public void receiveResult(Result result){
        _controller.receiveResult(result);
    }

    public void receiveProcessInfo(Process process){
        _controller.receiveProcessInfo(process);
    }

    public void sendUpdateInfoList(){
        _controller.updateProcessInfoListThread();

    }

    private void sendEndMessage(){
        _controller.updateGlobalTime(_RR.getTotalRunTime());
        _controller.updateTimeRemaining(0l);
        _controller.updateProcessTime(1.0);
        _controller.updateRunTime(0l);
        _controller.notifyEnded();
    }

    private void startGlobalTime(){
        this._start = System.currentTimeMillis();
    }

    public synchronized long getGlobalTime(){
        if(getStopGlobal()) return _sumTime;

        return _sumTime + System.currentTimeMillis() - this._start;
    }

    private char randomOperation(){
        switch(random.nextInt(5)){
            case 0: return '+';
            case 1: return '-';
            case 2: return '*';
            case 3: return '/';
            case 4: return '%';
        }

        return '+';
    }
}
