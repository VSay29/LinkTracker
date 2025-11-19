package linktracker.model;

import java.util.ArrayList;
import java.util.List;

public class WebPage {

    private String nombrePagina;
    private String url;
    private List<String> listaEnlaces;

    public WebPage(String nombrePagina, String url) {
        this.nombrePagina = nombrePagina;
        this.url = url;
        this.listaEnlaces = new ArrayList<>();
    }

    public String getNombrePagina() {
        return nombrePagina;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getListaEnlaces() {
        return listaEnlaces;
    }

    public void setListaEnlaces(List<String> listaEnlaces) {
        this.listaEnlaces = listaEnlaces;
    }

}
