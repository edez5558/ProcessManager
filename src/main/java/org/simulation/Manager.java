package org.simulation;

import java.util.Random;

public class Manager extends Thread{
    private Batch[] _batchs; 
    private int _numberProcess;
    private static Random random = new Random();
    private long _start;
    private long _sumTime = 0;
    private boolean _stopGlobal = false;

    private final Controller _controller;

    private Batch _currentBatch;

    private long _totalRunTime;
    public synchronized void sendInterruption(){
        if(_currentBatch != null)
            _currentBatch.setInterruption();
    }

    public synchronized void sendError(){
        if(_currentBatch != null)
            _currentBatch.setError();
    }

    public synchronized void sendPause(){
        if(_currentBatch != null && !_stopGlobal){
            _sumTime += System.currentTimeMillis() - _start;
            _currentBatch.setWait();
            _stopGlobal = true;
        }
    }

    public synchronized void sendContinue(){
        if(_currentBatch != null && _stopGlobal){
            _start = System.currentTimeMillis();
            _currentBatch.setContinue();
            _stopGlobal = false;
        }
    }

    Manager(Controller controller){
        _currentBatch = null;
        _controller = controller;
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
        _controller.updateTimeRemaining(0l);
        _controller.updateProcessTime(1.0);
        _controller.updateGlobalTime(_totalRunTime);
        _controller.updateRunTime(0l);
        _controller.notifyEnded();
    }

    public void paintBatch(){
        while(true){
            synchronized(Batch.class){
                if(_currentBatch.getState() == State.TERMINATED)
                    break;
            }

            _controller.updateGlobalTime(getGlobalTime());
            _controller.updateProcessTime(_currentBatch.getCurrentProcessProgress());
            _controller.updateTimeRemaining(_currentBatch.getCurrentProcessRemainingTime());
            _controller.updateRunTime(_currentBatch.getCurrentProcessRunTime());
            sendUpdateInfoList();

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    } 

    @Override
    public void run(){
        startGlobalTime();

        for(int i = 0; i < _batchs.length; i++){
            _controller.updateBatchRemaining(_batchs.length - (i+1));
            _controller.setIndexBatch(i);

            _currentBatch = _batchs[i];

            _currentBatch.start();

            paintBatch();

            this._totalRunTime += _currentBatch.getTotalRunTime();
        }
        
        sendEndMessage();
    }

    public int getNumberProcess(){
        return _numberProcess; 
    }

    private void startGlobalTime(){
        this._start = System.currentTimeMillis();
    }

    private long getGlobalTime(){
        if(_stopGlobal) return _sumTime;

        return _sumTime + System.currentTimeMillis() - this._start;
    }

    public int getTotalBatch(){
        return _batchs.length;
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
    private void fillBatchs(){
        for(Batch batch : _batchs){
            for(int i = 0; i < batch.getMaxIndex(); i++){
                batch.setProcessAt(i, new Process(
                                random.nextInt(18-7) + 7,
                            randomOperation(),
                            random.nextInt(20) + 1,
                            random.nextInt(20) + 1,
                            "Esperando..."
                        )
                        );
            }
        }
    }

    public int setTotalProcess(int numberProcess){
        int totalBatch = numberProcess/3;
        int nProcessBatch = 3;

        if(numberProcess% nProcessBatch != 0)
            totalBatch++;

        _batchs =  new Batch[totalBatch];
        _numberProcess = numberProcess;

        for(int i = 0; i < numberProcess/ nProcessBatch; i++)
            _batchs[i] = new Batch(nProcessBatch,i + 1,this);
        
        if(numberProcess% nProcessBatch != 0)
            _batchs[numberProcess/ nProcessBatch] = new Batch(numberProcess% nProcessBatch,totalBatch,this);

        fillBatchs();
        return totalBatch;
    }

    public void setProcessAt(int indexBatch,int index,Process process){
        _batchs[indexBatch].setProcessAt(index, process);
    }

    public int getMaxIndex(int indexBatch){
        return _batchs[indexBatch].getMaxIndex();
    }

    public Process getProcessAt(int indexBatch,int index){
        return _batchs[indexBatch].getIndex(index);
    }

    public boolean avaliableID(int ID){
        for(Batch batch : _batchs){
            if(batch.avaliableID(ID)) return false;
        }

        return true;
    }
}
