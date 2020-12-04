package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    public TreeView<Directory> treFolders;
    public TreeItem<Directory> treeRoot;
    public TableView<File> tblView;
    public TableColumn<File, String> colName;
    public TableColumn<File, LocalDateTime> colLastModified;
    public TableColumn<File, Integer> colSize;
    public TableColumn<File, String> colType;
    private ObservableList<File> fileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList = FXCollections.observableArrayList();
        tblView.setItems(fileList);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastModified.setCellValueFactory(new PropertyValueFactory<>("lastModifiedFormatted"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeFormatted"));

        Directory rootDirectory = new Directory(0, 0, "/");
        treeRoot = new TreeItem<>(rootDirectory, getFolderIcon());
        rootDirectory.setTreeItem(treeRoot);
        treFolders.setRoot(treeRoot);
        treFolders.getSelectionModel().select(treeRoot);

        readHeaderFile();
        updateTableView();
    }

    public void handleBtnCreateFolderClick() {
        TextInputDialog textInputDialog = new TextInputDialog("Nova pasta");
        textInputDialog.setTitle("Criando uma nova pasta");
        textInputDialog.setHeaderText("Digite o nome da nova pasta:");
        textInputDialog.showAndWait();
        String result = textInputDialog.getResult();
        if (result != null && !result.isEmpty()) {
            TreeItem<Directory> treeItem = treFolders.getSelectionModel().getSelectedItem();

            try {
                TreeItem<Directory> newTreeItem = new TreeItem<>(new Directory(treeItem.getValue().getId(), result.replaceAll("\\|", "")), getFolderIcon());
                treeItem.getChildren().add(newTreeItem);
                treeItem.setExpanded(true);

                treFolders.getSelectionModel().select(newTreeItem);
                updateTableView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ImageView getFolderIcon() {
        Image folderImage = new Image(getClass().getResourceAsStream("folderIcon.png"));
        ImageView folderIcon = new ImageView(folderImage);
        folderIcon.setFitHeight(16);
        folderIcon.setFitWidth(16);
        return folderIcon;
    }

    public void handleBtnSubmitFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo");
        Stage fileChooserWindow = new Stage();
        java.io.File selectedFile = fileChooser.showOpenDialog(fileChooserWindow);
        if (selectedFile == null) return;
        try {
            TreeItem<Directory> treeItem = treFolders.getSelectionModel().getSelectedItem();
            long contentFilePosition = 0;
            java.io.File contentFile = new java.io.File("content_file.bin");
            if (contentFile.exists()) contentFilePosition = contentFile.length();

            File file = new File(selectedFile.getName(), Files.probeContentType(Paths.get(selectedFile.getPath())), selectedFile.lastModified(), contentFilePosition, selectedFile.length(), treeItem.getValue().getId());
            file.insert(Files.readAllBytes(Paths.get(selectedFile.getPath())));
            treeItem.getValue().insertFile(file);

            updateTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleTreFoldersClick() {
        updateTableView();
    }

    private void updateTableView() {
        List<File> files = treFolders.getSelectionModel().getSelectedItem().getValue().getFiles();
        fileList.setAll(files);

        tblView.setRowFactory(tv -> {
            TableRow<File> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    try {
                        File file = row.getItem();
                        if (file.getType().toUpperCase().contains("text".toUpperCase())) {
                            Dialog<ButtonType> dialog = new Dialog<>();
                            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                            dialog.setTitle("Exibindo o conteúdo do arquivo " + file.getName());
                            dialog.setContentText(new String(file.getFileContent()));
                            dialog.setResizable(true);
                            dialog.showAndWait();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!row.isEmpty()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem delete = new MenuItem("Excluir");
                    contextMenu.getItems().addAll(delete);
                    row.setContextMenu(contextMenu);

                    delete.setOnAction(actionEvent -> {
                        try {
                            File file = row.getItem();
                            file.delete();
                            Directory directory = treFolders.getSelectionModel().getSelectedItem().getValue();
                            directory.remove(file);

                            updateTableView();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            return row;
        });
    }

    public void handleTreFoldersKeyReleased() {
        updateTableView();
    }

    private void readHeaderFile() {
        java.io.File headerFile = new java.io.File("header.txt");
        if (headerFile.exists()) {
            List<Directory> directories = Directory.getDirectories();
            try {
                Files.readAllLines(Paths.get(headerFile.getPath())).forEach(line -> {
                    Pattern directoryPattern = Pattern.compile("([0-9]*)D\\|([0-9]*)D\\|(.*)");
                    Matcher directoryMatcher = directoryPattern.matcher(line);
                    if (directoryMatcher.matches()) {
                        long parentDirectoryId = Long.parseLong(directoryMatcher.group(1));
                        long id = Long.parseLong(directoryMatcher.group(2));
                        String name = directoryMatcher.group(3);
                        Directory parentDirectory = directories.stream().filter(directory -> directory.getId() == parentDirectoryId).findAny().orElse(null);
                        if (parentDirectory != null) {
                            Directory newDirectory = new Directory(id, parentDirectoryId, name);

                            TreeItem<Directory> newTreeItem = new TreeItem<>(newDirectory, getFolderIcon());
                            parentDirectory.getTreeItem().getChildren().add(newTreeItem);
                            newDirectory.setTreeItem(newTreeItem);
                        }
                    }

                    Pattern filePattern = Pattern.compile("([0-9]*)D\\|([0-9]*)F\\|(.*)\\|(.*)\\|([0-9]*)\\|([0-9]*)\\|([0-9]*)");
                    Matcher fileMatcher = filePattern.matcher(line);
                    if (fileMatcher.matches()) {
                        long directoryId = Long.parseLong(fileMatcher.group(1));
                        long id = Long.parseLong(fileMatcher.group(2));
                        String name = fileMatcher.group(3);
                        String type = fileMatcher.group(4);
                        long lastModified = Long.parseLong(fileMatcher.group(5));
                        long startPosition = Long.parseLong(fileMatcher.group(6));
                        long size = Long.parseLong(fileMatcher.group(7));

                        Directory directory = directories.stream().filter(directoryFilter -> directoryFilter.getId() == directoryId).findAny().orElse(null);
                        if (directory != null) {
                            File file = new File(id, name, type, lastModified, startPosition, size, directoryId);
                            directory.insertFile(file);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            long nextDirectoryId = directories.stream().mapToLong(Directory::getId).max().orElse(0) + 1;
            Directory.updateIncrementId(nextDirectoryId);

            long nextFileId = directories.stream().flatMapToLong(directory -> directory.getFiles().stream().mapToLong(File::getId)).max().orElse(0) + 1;
            File.updateIncrementId(nextFileId);
        }
    }
}
