package co.rabbitmq;

import java.io.Serializable;

public class Cliente implements Serializable {
    private String id;
    private String nombre;
    private String origen;
    private String destino;
    private String estado;
    private String zona;

    public Cliente() {}

    public Cliente(String id, String nombre, String origen, String destino, String estado, String zona) {
        this.id = id;
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
        this.zona = zona;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", estado='" + estado + '\''  +
                ", zona='" + zona + '\'' +
                '}';
    }
}
