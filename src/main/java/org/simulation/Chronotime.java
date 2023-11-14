package org.simulation;

import java.util.ArrayList;

public class Chronotime extends Thread{
    ArrayList<Long> _blockedStart;
    ArrayList<Long> _blockedSum;
    private boolean _running;
    private long _objectiveTime;
    private long _currentExpectedEnd;
    private volatile boolean _wait;
    private Manager _manager;
    private int dynamicIndex;
    Chronotime(Manager manager,long time){
        _running = true;
        _objectiveTime = time;
        _wait = false;
        _manager = manager;
        _blockedSum = new ArrayList<>();
        _blockedStart = new ArrayList<>();
        dynamicIndex = 0;
    }

    public int add(){
        synchronized (RR.class){
            _blockedSum.add(0l);
            _blockedStart.add(System.currentTimeMillis());
        }

        if(getState() == State.WAITING){
            awake();
        }else{
            System.out.println(getState());
        }
        return _blockedStart.size() - 1 + dynamicIndex;
    }

    public synchronized void startWaiting(){
        long aux = System.currentTimeMillis();
        _currentExpectedEnd = 0;
        for(int i = 0; i < _blockedStart.size(); i++){
            _blockedSum.set(i,
                    _blockedSum.get(i) + (aux - _blockedStart.get(i))
            );
        }

        setWait(true);
    }

    public synchronized void setWait(boolean value){
        _wait = value;
    }

    public synchronized boolean getWait(){
        return _wait;
    }

    public synchronized  void awake(){
        long aux = System.currentTimeMillis();

        for(int i = 0; i < _blockedStart.size(); i++){
            _blockedStart.set(i,aux);
        }

        setWait(false);
        synchronized (this){
            this.notify();
        }
    }

    public long getProcessTime(Process process){
        int index = process.getIndex() - dynamicIndex;
        if(index < 0 || index >= _blockedStart.size()) return 0;

        long aux = _blockedSum.get(index ) + (System.currentTimeMillis() - _blockedStart.get(index));

        return aux > _objectiveTime? _blockedSum.get(index) : aux;
    }

    public void endThread(){
        _running = false;
        awake();
    }


    @Override
    public void run(){
        while(_running){
            if(_blockedStart.isEmpty()){
                dynamicIndex = 0;
                try {
                    synchronized (this){
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(!_running) break;

                continue;
            }

            try {
                Thread.sleep(10); // for 100 FPS
            } catch (InterruptedException ignore) {
            }

            if(getWait()){
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if(!getWait() && _currentExpectedEnd == 0){
                _currentExpectedEnd = System.currentTimeMillis() +
                            (_objectiveTime - _blockedSum.get(0) - (System.currentTimeMillis() - _blockedStart.get(0)));
            }

            if(!getWait() && System.currentTimeMillis() >= _currentExpectedEnd){
                synchronized (RR.class){
                    _manager.changeToReady();
                    dynamicIndex += 1;
                    _currentExpectedEnd = 0;
                    _blockedStart.remove(0);
                    _blockedSum.remove(0);
                }
            }
        }
    }
}
