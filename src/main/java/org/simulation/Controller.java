package org.simulation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class Controller {
    @FXML
    private Button btnNumberSet;

    @FXML
    private Button btnProcessSet;

    @FXML
    private Label lbCurrentLote;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtLeftNumber;

    @FXML
    private TextField txtNameProgrammer;

    @FXML
    private TextField txtNumberProcess;

    @FXML
    private TextField txtRightNumber;

    @FXML
    private TextField txtTime; 

    @FXML
    private Button btnLeft;

    @FXML
    private Button btnRight;

    @FXML
    private FlowPane plProcess;

    @FXML
    private ChoiceBox<String> cbOperation; 

    @FXML
    private Button btnExecute;
    
    @FXML
    private Label lbGlobalTime;

    @FXML
    private Label lbTimeRest;

    @FXML
    private Label lbBatchRest;

    @FXML
    private Label lbRunTime;

    @FXML
    private TableColumn<Result, Integer> tcID;

    @FXML
    private TableColumn<Result, Integer> tcLote;

    @FXML
    private TableColumn<Result, String> tcResult;

    @FXML
    private TableView<Result> tvFinished;

    @FXML
    private ProgressBar pbProcess;

    private Manager _manager;

    private Pane previousIndex = null;

    private int currentBatchIndex = 0;
    private int currentProcessIndex = 0;

    private Process currentProcess = null;

    private boolean _isSelectNumber;

    private ObservableList<Result> _results;


    private void disableProcessInfo(){
        this.txtLeftNumber.setDisable(true);
        this.txtRightNumber.setDisable(true);
        this.txtID.setDisable(true);
        this.txtTime.setDisable(true);
        this.txtNameProgrammer.setDisable(true);
        this.cbOperation.setDisable(true);
    }

    private void enableProcessInfo(){
        this.txtLeftNumber.setDisable(false);
        this.txtRightNumber.setDisable(false);
        this.txtID.setDisable(false);
        this.txtTime.setDisable(false);
        this.txtNameProgrammer.setDisable(false);
        this.cbOperation.setDisable(false);
    }
    private void simpleAlertIncorrectInput(String msg){
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Dato incorrecto");
        alert.setContentText(msg);
        alert.showAndWait();
    }
    

    private void cleanProcessInfo(){
        this.txtLeftNumber.setText("");
        this.txtRightNumber.setText("");
        this.txtID.setText("");
        this.txtID.setDisable(false);
        this.txtTime.setText("");
        this.txtNameProgrammer.setText("");
        this.cbOperation.setValue(null);
    }

    private void updateProcessInfo(Pane pl,int index){
        Label lbID = (Label)pl.getChildren().get(1);
        Label lbEditable = (Label)pl.getChildren().get(3);
        Label lbStatus = (Label)pl.getChildren().get(4);

        Process current = _manager.getProcessAt(currentBatchIndex, index);

        if(current == null){
            lbID.setText("");
            lbStatus.setText("");

            if(index < _manager.getMaxIndex(currentBatchIndex))
                lbEditable.setText("Sin datos");
            else
                lbEditable.setText("Sin proceso");

            return;
        }

        lbID.setText(Integer.toString(current.getID()));

        lbEditable.setText("");
        lbStatus.setText(current.getStatus());
    }

    public void updateProcessInfoList(){
        int index = 0;

        for (Node node : plProcess.getChildren()) {
            Pane aux = (Pane)node;
            updateProcessInfo(aux, index);

            index++;
        }
    }


    private void updateIndexBatch(int offset){
        currentBatchIndex += offset;

        if(currentBatchIndex < 0) currentBatchIndex = 0;
        else
        if(currentBatchIndex >= _manager.getTotalBatch()) currentBatchIndex = _manager.getTotalBatch() - 1;

        lbCurrentLote.setText(
            String.format("%d/%d", currentBatchIndex + 1,_manager.getTotalBatch())
        );

        updateProcessInfoList();
    }

    private void fillProcessData(Process process){

        if(process == null){
            cleanProcessInfo();
            return;
        }

        
        txtID.setText(Integer.toString(process.getID()));
        txtID.setDisable(true);

        txtTime.setText(Integer.toString(process.getTime()));
        txtNameProgrammer.setText(process.getNameProgrammer());
        txtLeftNumber.setText(Integer.toString(process.getLeftNumber()));
        txtRightNumber.setText(Integer.toString(process.getRightNumber()));
        cbOperation.setValue(Character.toString(process.getOperation()));
    }

    private static String millisecondsToTimeString(Long milliseconds){
        return  String.format("%1$02d:%2$02d", 
                    milliseconds/1000,
                    (int)Math.round((milliseconds%1000)/1000.0 * 60)
                );
    }
    public void updateGlobalTime(Long milliseconds){
        /*
         * Javafx no permite modificar componentes de la GUI con threads externos
         */
        Platform.runLater(() -> {
            lbGlobalTime.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void updateTimeRemaining(Long milliseconds){
        Platform.runLater(() -> {
            lbTimeRest.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void updateRunTime(Long milliseconds){
        Platform.runLater(() -> {
            lbRunTime.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void setIndexBatch(int index){
        Platform.runLater(() -> {
            updateIndexBatch(index - currentBatchIndex);
        });
    }

    public void updateProcessInfoListThread(){
        Platform.runLater(() -> {
            updateProcessInfoList();
        });
    }

    public void notifyEnded(){
        Platform.runLater(( ) -> {
            this._isSelectNumber = true;
            this.btnLeft.setDisable(false);
            this.btnRight.setDisable(false);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Listo!!!");
            alert.setContentText("Todos los procesos en los lotes sean ejecutado");
            alert.showAndWait();
        });
    } 

    public void updateBatchRemaining(int value){
        Platform.runLater(() -> {
            lbBatchRest.setText(Integer.toString(value));
        });
    }

    public void updateProcessTime(double progress){
        Platform.runLater(() -> {
            pbProcess.setProgress(progress);
        });
    }

    public void receiveResult(Result result){
        _results.add(result);
    }

    public void receiveProcessInfo(Process process){
        Platform.runLater(() -> {
            fillProcessData(process);
        });
    }

    private EventHandler<Event> createProcessClickHandler(){
        /*
         * Cuando el usuario de click en uno de los procesos que se encuentren en el panel izquierdo
         */
        return new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                if(_manager == null || !_isSelectNumber) return;

                Pane clickPanel = (Pane)event.getSource();

                currentProcessIndex = Integer.parseInt(
                    clickPanel.getId().replace("plProcess","")
                ) - 1;


                if(currentProcessIndex >= _manager.getMaxIndex(currentBatchIndex)){
                    previousIndex.getStyleClass().clear();
                    previousIndex.getStyleClass().add("process-panel");
                    cleanProcessInfo();
                    return;
                }


                if(previousIndex != null && previousIndex != clickPanel){
                    previousIndex.getStyleClass().clear();
                    previousIndex.getStyleClass().add("process-panel");
                }

                clickPanel.getStyleClass().clear();
                clickPanel.getStyleClass().add("current-process-panel");

                previousIndex = clickPanel;

                currentProcess = _manager.getProcessAt(currentBatchIndex, currentProcessIndex);
                fillProcessData(currentProcess);
            } 
        };
    }

    @FXML
    private void initialize(){
        tcID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        tcResult.setCellValueFactory(new PropertyValueFactory<>("resultado"));

        disableProcessInfo();
        btnProcessSet.setDisable(true);

        _results = FXCollections.observableArrayList();

        tvFinished.setItems(_results);

        _manager = new Manager(this);
        _isSelectNumber = false;

        btnNumberSet.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                try{
                    int number = Integer.parseInt(txtNumberProcess.getText());

                    if(number <= 0){
                        simpleAlertIncorrectInput("Ingresa un numero mayor a 0");
                        return;
                    }

                    btnNumberSet.setDisable(true);
                    txtNumberProcess.setDisable(true);

                    _manager.setTotalProcess(number);
                    _isSelectNumber = true;
                    updateIndexBatch(0);

                    btnLeft.setDisable(false);
                    btnRight.setDisable(false);
                    btnExecute.setDisable(false);

                    enableProcessInfo();
                    btnProcessSet.setDisable(false);

                    ObservableList<String> aux = cbOperation.getItems();

                    aux.add("+");
                    aux.add("-");
                    aux.add("*");
                    aux.add("/");
                    aux.add("%");
                    aux.add("^");

                }catch(NumberFormatException e){
                    simpleAlertIncorrectInput("Ingresa un numero");
                }
            }
        });


        btnProcessSet.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                try{
                    if(_manager == null || currentBatchIndex == -1) return;

                    if(txtNameProgrammer.getText().isBlank()){
                        simpleAlertIncorrectInput("Campo nombre de programador vacio.");
                        return;
                    }

                    if(txtID.getText().isBlank()){
                        simpleAlertIncorrectInput("Campo ID vacio.");
                        return;
                    }

                    if(txtLeftNumber.getText().isBlank()){
                        simpleAlertIncorrectInput("Campo primer numero vacio.");
                        return;
                    }

                    if(txtRightNumber.getText().isBlank()){
                        simpleAlertIncorrectInput("Campo segundo numero vacio.");
                        return;
                    }

                    if(cbOperation.getValue() == null){
                        simpleAlertIncorrectInput("Campo operacion no seleccionado.");
                        return;
                    }
                    
                    int id = Integer.parseInt(txtID.getText());
                    int time = Integer.parseInt(txtTime.getText());
                    int leftNumber = Integer.parseInt(txtLeftNumber.getText());
                    int rightNumber = Integer.parseInt(txtRightNumber.getText());
                    char operation = cbOperation.getValue().charAt(0);
                    if(currentProcess == null && !_manager.avaliableID(id)){
                        simpleAlertIncorrectInput("ID ya ingresado.");
                        return;
                    }


                    if(rightNumber == 0 && (operation == '/' || operation == '%')){
                        simpleAlertIncorrectInput("No se puede dividir entre 0.");
                        return; 
                    }

                    if(currentProcess == null){
                        Process newProcess =    new Process(
                                                        txtNameProgrammer.getText(),
                                                        id,
                                                        time,
                                                        cbOperation.getValue().charAt(0),
                                                        leftNumber,
                                                        rightNumber,
                                                        "Pendiente...");

                        _manager.setProcessAt(currentBatchIndex, currentProcessIndex,newProcess);

                        currentProcess = newProcess; 

                        updateProcessInfoList();
                        return;
                    }

                    currentProcess.setNameProgrammer(txtNameProgrammer.getText());
                    currentProcess.setLeftNumber(leftNumber);
                    currentProcess.setRightNumber(rightNumber);
                    currentProcess.setTime(time);
                    currentProcess.setOperation(cbOperation.getValue().charAt(0));
                }catch(NumberFormatException e){
                    simpleAlertIncorrectInput("Ingresa un numero");
                }
            }
        });

        EventHandler<Event> event = createProcessClickHandler();

        for (Node node : plProcess.getChildren()) {
            ((Pane)node).addEventHandler(MouseEvent.MOUSE_CLICKED, event);
        }

        btnLeft.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                updateIndexBatch(-1);
                currentProcess = null;

                if(previousIndex == null) return;

                previousIndex.getStyleClass().clear();
                previousIndex.getStyleClass().add("process-panel");
                cleanProcessInfo();
            } 
        });

        btnRight.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                updateIndexBatch(1);
                currentProcess = null;

                if(previousIndex == null) return;

                previousIndex.getStyleClass().clear();
                previousIndex.getStyleClass().add("process-panel");
                cleanProcessInfo();
            } 
        });

        btnExecute.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                /*
                 * Verificar que todo los procesos esten registrados
                 */
                for(int i = 0; i < _manager.getNumberProcess(); i++){
                    if(_manager.getProcessAt(i/4, i%4) != null) continue;

                    simpleAlertIncorrectInput(String.format("Faltan datos en el lote %1d con el indice %2d",i/4 + 1,i%4));
                    return;
                }

                btnProcessSet.setDisable(true); 
                btnProcessSet.setVisible(false);
                btnExecute.setDisable(true);

                previousIndex.getStyleClass().clear();
                previousIndex.getStyleClass().add("process-panel");

                btnLeft.setDisable(true);
                btnRight.setDisable(true);

                disableProcessInfo();
                _isSelectNumber = false;

                _manager.start();
            } 
        });
    }
}
