package co.rabbitmq;

import java.io.Serializable;

public class Conductor implements Serializable {
    private String id;
    private String nombre;
    private String vehiculo;
    private String mensaje;
    private String disponibilidad;
    private String zona;

    public Conductor() {}

    public Conductor(String id, String nombre, String vehiculo, String mensaje, String disponibilidad, String zona) {
        this.id = id;
        this.nombre = nombre;
        this.vehiculo = vehiculo;
        this.mensaje = mensaje;
        this.disponibilidad = disponibilidad;
        this.zona = zona;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getVehiculo() { return vehiculo; }
    public void setVehiculo(String vehiculo) { this.vehiculo = vehiculo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }

    public String getZona(){
        return zona;
    }

    public void setZona(String zona){
        this.zona = zona;
    }
    @Override
    public String toString() {
        return "Conductor{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", vehiculo='" + vehiculo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", disponibilidad='" + disponibilidad + '\'' +
                ", zona='" + zona + '\'' +
                '}';
    }
}
