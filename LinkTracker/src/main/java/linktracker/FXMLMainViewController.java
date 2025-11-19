package linktracker;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import linktracker.model.WebPage;
import linktracker.utils.FileUtils;
import linktracker.utils.LinkReader;
import linktracker.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FXMLMainViewController {

    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu mnFile;
    @FXML
    private Menu mnProcess;
    @FXML
    private MenuItem mnItemLoadFile;
    @FXML
    private MenuItem mnItemExit;
    @FXML
    private MenuItem mnItemStart;
    @FXML
    private MenuItem mnItemClear;
    @FXML
    private ListView<String> listPages;
    @FXML
    private ListView<String> listLinks;
    @FXML
    private Label lblTotalPages;
    @FXML
    private Label lblProcessed;
    @FXML
    private Label lblTotalLinks;
    @FXML
    private Label lblNumTotalPages;
    @FXML
    private Label lblNumProcessed;
    @FXML
    private Label lblNumTotalLinks;

    private static List<WebPage> wps =  new ArrayList<WebPage>();
    private static AtomicInteger totalLinks = new AtomicInteger();
    private static AtomicInteger totalProcessed = new AtomicInteger();
    private ScheduledService<Boolean> schedServ;
    private ThreadPoolExecutor ejecutor;

    @FXML
    public void exitWindow(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void loadFile(ActionEvent actionEvent) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de texto");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Texto", "*.txt"));

        Stage stage = (Stage) menuBar.getScene().getWindow();

        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if(archivoSeleccionado != null) {
            wps = FileUtils.loadPages(archivoSeleccionado.toPath());
            if(!wps.isEmpty()) MessageUtils.showMessage(wps.size() + " pages found", "File loaded");
            else MessageUtils.showMessage("No pages found", "File loaded");
            actualizarNumTotalPages(wps);
        } else { MessageUtils.showError("File not loaded", "No file selected"); }

    }

    private void actualizarNumTotalPages(List<WebPage> wps) {
        lblNumTotalPages.setText(Integer.toString(wps.size()));
    }

    public void clearAll(ActionEvent actionEvent) {
        limpiar();
    }

    public void start(ActionEvent actionEvent) {
        if (wps.isEmpty()) {
            MessageUtils.showError("No URL list loaded", "Process error");
            return;
        } else {
            if (totalProcessed.get() > 0 || totalLinks.get() > 0) {
                MessageUtils.showError("Se requiere limpiar el proceso", "Hay un proceso en ejecución");
                return;
            } else {
                ejecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                List<Callable<WebPage>> callables = new ArrayList<>();
                for (WebPage wp : wps) callables.add(procesarPagina(wp));
                List<Future<WebPage>> futures = new ArrayList<>();
                for (Callable<WebPage> c : callables) futures.add(ejecutor.submit(c));
                ejecutor.shutdown();
                schedServ.restart();
            }
        }
    }

    public void seleccionarPagina(MouseEvent mouseEvent) {
        if(!listPages.getItems().isEmpty()) {
            String paginaSeleccionada = listPages.getSelectionModel().getSelectedItem();
            rellenarListaEnlaces(paginaSeleccionada);
        }
    }

    /**
     * Función para procesar las páginas web
     * @param wp: Página web que será procesada
     * @return: Devolverá la página web procesada
     */

    private Callable<WebPage> procesarPagina(WebPage wp) {
        return () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                wp.setListaEnlaces(LinkReader.getLinks(wp.getUrl()));
                totalLinks.addAndGet(wp.getListaEnlaces().size());
                totalProcessed.incrementAndGet();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Task interrupted", e);
            }
            return wp;
        };
    }

    public void initialize() {
        schedServ = new ScheduledService<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return ejecutor.isTerminated();
                    }
                };
            }
        };

        schedServ.setDelay(Duration.millis(500));
        schedServ.setPeriod(Duration.millis(100));
        schedServ.setOnSucceeded(event -> {
            lblNumTotalLinks.setText(Integer.toString(totalLinks.get()));
            lblNumProcessed.setText(Integer.toString(totalProcessed.get()));
            if(schedServ.getValue()) {
                schedServ.cancel();
                for(WebPage wp : wps) listPages.getItems().add(wp.getNombrePagina());
            }
        });

    }

    private void limpiar() {
        if(!wps.isEmpty()) wps.clear();
        if(listPages != null) listPages.getItems().clear();
        if(listLinks != null) listLinks.getItems().clear();
        lblNumTotalPages.setText(Integer.toString(0));
        lblNumProcessed.setText(Integer.toString(0));
        lblNumTotalLinks.setText(Integer.toString(0));
        totalLinks.set(0);
        totalProcessed.set(0);
    }

    private void rellenarListaEnlaces(String nombrePagina) {
        listLinks.getItems().clear();
        for(WebPage wp : wps) {
            if(wp.getNombrePagina().equals(nombrePagina)) {
                if(!wp.getListaEnlaces().isEmpty()) {
                    for (String link : wp.getListaEnlaces()) listLinks.getItems().add(link);
                    break;
                } else MessageUtils.showMessage("Esta página no tiene enlaces", "No enlaces found");
            }
        }
    }
}
