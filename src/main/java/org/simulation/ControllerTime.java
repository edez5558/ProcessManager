package org.simulation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ControllerTime {
    @FXML
    private TableColumn<Process, Double> tcInfoEspera;

    @FXML
    private TableColumn<Process, Double> tcInfoFinalizacion;

    @FXML
    private TableColumn<Process, Double> tcInfoID;

    @FXML
    private TableColumn<Process, Double> tcInfoLlegada;

    @FXML
    private TableColumn<Process, Double> tcInfoRespuesta;

    @FXML
    private TableColumn<Process, String> tcInfoResultado;

    @FXML
    private TableColumn<Process, Double> tcInfoRetorno;

    @FXML
    private TableColumn<Process, Double> tcInfoServicio;

    @FXML
    private TableColumn<Process, Double> tcInfoRestante;

    @FXML
    private TableView<Process> tvInfo;

    @FXML
    private Button btnOk;

    private ObservableList<Process> _view;

    private TableCell<Process,Double> getCellStyle(){
        return new TableCell<Process,Double>(){
            @Override
            protected void updateItem(Double balance, boolean empty){
                super.updateItem(balance,empty);
                if(empty) {
                    setText("");
                }else if(balance == null){
                    setText("Nulo");
                }else{
                    if(balance < 0)
                        setText("No aplica");
                    else
                        setText(String.format("%.2f",balance));
                }
            }};
    }
    public void refrestTable(){
        Platform.runLater(() -> {
            tvInfo.refresh();
        });
    }
    public void setResults(ObservableList<Process> results){
        _view = results;

        tcInfoLlegada.setCellValueFactory(new PropertyValueFactory<>("timeEnter"));
        tcInfoLlegada.setCellFactory(tc -> getCellStyle());

        tcInfoFinalizacion.setCellValueFactory(new PropertyValueFactory<>("timeStop"));
        tcInfoFinalizacion.setCellFactory(tc -> getCellStyle());

        tcInfoRetorno.setCellValueFactory(new PropertyValueFactory<>("timeReturn"));
        tcInfoRetorno.setCellFactory(tc -> getCellStyle());

        tcInfoRespuesta.setCellValueFactory(new PropertyValueFactory<>("timeRespond"));
        tcInfoRespuesta.setCellFactory(tc -> getCellStyle());

        tcInfoEspera.setCellValueFactory(new PropertyValueFactory<>("timeWaiting"));
        tcInfoEspera.setCellFactory(tc -> getCellStyle());

        tcInfoServicio.setCellValueFactory(new PropertyValueFactory<>("timeService"));
        tcInfoServicio.setCellFactory(tc -> getCellStyle());

        tcInfoID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        tcInfoResultado.setCellValueFactory(new PropertyValueFactory<>("textResult"));

        tcInfoRestante.setCellValueFactory(new PropertyValueFactory<>("timeRemaining"));
        tcInfoRestante.setCellFactory(tc -> getCellStyle());

        tvInfo.setItems(_view);
    }

    @FXML
    private void initialize(){
        btnOk.addEventHandler(MouseEvent.MOUSE_CLICKED,new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                Stage stage = (Stage) btnOk.getScene().getWindow();

                stage.hide();
            }
        }
        );
    }
}
