package co.rabbitmq;

import java.io.Serializable;

public class Asignacion implements Serializable {
    private Cliente cliente;
    private Conductor conductor;
    public Asignacion() {}

    public Asignacion(Cliente cliente, Conductor conductor) {
        this.cliente = cliente;
        this.conductor = conductor;
    }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Conductor getConductor() { return conductor; }
    public void setConductor(Conductor conductor) { this.conductor = conductor; }

    @Override
    public String toString() {
        return "Asignacion{" +
                "cliente=" + cliente +
                ", conductor=" + conductor +
                '}';
    }
}
