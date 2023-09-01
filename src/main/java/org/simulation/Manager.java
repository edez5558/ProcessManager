package org.simulation;

public class Manager extends Thread{
    private Batch[] _batchs; 
    private int _numberProcess;

    private long _start;
    private int _currentTop;

    private Controller _controller;

    private Batch _currentBatch;

    private long _totalRunTime;

    Manager(Controller controller){
        _currentTop = 0;
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


    public Process getTopProcess(){
        if(_currentTop >= _numberProcess) return null;

        Process tmp = _batchs[_currentTop/4].getIndex(_currentTop%4);

        _currentTop++;
        return tmp;
    }

    public int getNumberProcess(){
        return _numberProcess; 
    }

    private void startGlobalTime(){
        this._start = System.currentTimeMillis();
    }

    private long getGlobalTime(){
        return System.currentTimeMillis() - this._start;
    }

    public int getTotalBatch(){
        return _batchs.length;
    }

    public int setTotalProcess(int numberProcess){
        int totalBatch = numberProcess/4;

        if(numberProcess%4 != 0)
            totalBatch++;

        _batchs =  new Batch[totalBatch];
        _numberProcess = numberProcess;

        for(int i = 0; i < numberProcess/4; i++)
            _batchs[i] = new Batch(4,i + 1,this);
        
        if(numberProcess%4 != 0)
            _batchs[numberProcess/4] = new Batch(numberProcess%4,totalBatch,this);

        return totalBatch;
    }
    
    public boolean isEmptyProcess(int index){
        return _batchs[index/4].getIndex(index%4) != null;
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
        for(int i = 0; i < _batchs.length; i++){
            if(!_batchs[i].avaliableID(ID)) return false;
        }

        return true;
    }
}
