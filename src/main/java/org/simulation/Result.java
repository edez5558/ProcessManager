package org.simulation;

public class Result {
    public int lote;
    public int id;
    public String resultado;
    public double tiempoLlegada;
    public double tiempoFinalizacion;
    public double tiempoRetorno;
    public double tiempoRespuesta;
    public double tiempoEspera;
    public double tiempoServicio;
    public long timepoFinalizacionMilli;

    public long getTimepoFinalizacionMilli() {
        return timepoFinalizacionMilli;
    }

    public void setTimepoFinalizacionMilli(long timepoFinalizacionMilli) {
        this.timepoFinalizacionMilli = timepoFinalizacionMilli;
    }

    public double getTiempoLlegada() {
        return tiempoLlegada;
    }

    public void setTiempoLlegada(double tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public double getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }

    public void setTiempoFinalizacion(double tiempoFinalizacion) {
        this.tiempoFinalizacion = tiempoFinalizacion;
    }

    public double getTiempoRetorno() {
        return tiempoRetorno;
    }

    public void setTiempoRetorno(double tiempoRetorno) {
        this.tiempoRetorno = tiempoRetorno;
    }

    public double getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    public void setTiempoRespuesta(double tiempoRespuesta) {
        this.tiempoRespuesta = tiempoRespuesta;
    }

    public double getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(double tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public double getTiempoServicio() {
        return tiempoServicio;
    }

    public void setTiempoServicio(double tiempoServicio) {
        this.tiempoServicio = tiempoServicio;
    }

    public int getLote() {
        return lote;
    }
    public void setLote(int lote) {
        this.lote = lote;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getResultado() {
        return resultado;
    }
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public void setProcessInfo(Process process){
        setTiempoLlegada(process.getTimeEnter());
        setTiempoFinalizacion(process.getTimeStop());
        setTiempoRetorno(process.getTimeReturn());
        setTiempoRespuesta(process.getTimeRespond());
        setTiempoEspera(process.getTimeWaiting());
        setTiempoServicio(process.getTimeService());
        timepoFinalizacionMilli = process.getTimeStopMilli();
    }
}
