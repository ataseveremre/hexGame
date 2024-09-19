package edu.erciyes.vp.hexgame;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class GameModel {

    //------------------------------------------------------------------------------------------------------------------
    //Kullandığımız değişkenler:
    //------------------------------------------------------------------------------------------------------------------

    @FXML
    private boolean kirmiziSira = true; //Oyuna ilk başlayan kırmızı belirlendi,sonradan swap kuralı ile değiştirilebilir.
    @FXML
    private boolean firstMoveMade = false; //İlk hareket yapıldı mı onu belirler,evetse sırayı değiştiren metoda yönelir.
    @FXML
    private boolean swapOptionAvailable = true; //Swap butonu kullanılabilirliğini belirler
    @FXML
    private Button[][] hexButtons; //Oyun tahtasındaki hexagon şekilleri
    @FXML
    private int currentSize; //Oyun tahtasının boyutu 5x5,11x11,17x17
    @FXML
    private int redWins = 0, blueWins = 0;//Kırmızı ve mavi oyuncularının kazandığı round sayısı
    @FXML
    private int firstMoveRow, firstMoveCol; //İlk hareketin yapıldığı satır ve sütunun indeksi
    @FXML
    private static final double hexSize = 17; //Ekranda görülen hexagonal şekillerin büyüklüğü

    //------------------------------------------------------------------------------------------------------------------
    //JavaFX Scene elemanları:
    //------------------------------------------------------------------------------------------------------------------

    @FXML
    private ImageView imageView; //Anamenüdeki arkaplan resmi
    @FXML
    private VBox vboxStart, mainVBox; //Anamenü ve oyun ekranlarını içeren vboxlar
    @FXML
    private GridPane gridPane; //Oyun tahtasının yer aldığı gridpane
    @FXML
    private RadioButton hex5x5, hex11x11, hex17x17;//Tahta boyutunu seçtiğimiz radio buttonlar
    @FXML
    private Button restartButton, playerButton, swapButton, mainMenuButton;//Oyun içi seçenek butonları
    @FXML
    private Label redWinsLabel, blueWinsLabel; //Skor tablosundaki yazı label'ları

    //------------------------------------------------------------------------------------------------------------------
    //Metodlar:
    //------------------------------------------------------------------------------------------------------------------
      //Oyun başlamadan önce JavaFX tarafından açılan ilk metod, initialize() metodu

    @FXML
    public void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        hex5x5.setToggleGroup(toggleGroup);
        hex11x11.setToggleGroup(toggleGroup);
        hex17x17.setToggleGroup(toggleGroup);
        imageView.setImage(new Image("file:src\\main\\resources\\images\\image.png"));
        updateWinsLabels();
    } //Ana menüdeki oyun tahtasının 5x5, 11x11, 17x17 mi olacağını seçtiğimiz yer

    //------------------------------------------------------------------------------------------------------------------
        //Oyun kurucu metodlar

    @FXML
    private void createHexagonButtons(int size) {
        gridPane.getChildren().clear();
        hexButtons = new Button[size][size];
        currentSize = size;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Polygon hexagon = createHexagon(hexSize);
                hexagon.setFill(Color.TRANSPARENT);
                hexagon.setStroke(Color.BLACK);

                Button hexButton = new Button();
                hexButton.setShape(hexagon);
                hexButton.setMinSize(hexSize * 2, hexSize * 2);
                hexButton.setMaxSize(hexSize * 2, hexSize * 2);
                hexButtons[row][col] = hexButton;

                int finalRow = row;
                int finalCol = col;

                hexButton.setOnAction(e -> {
                    if (kirmiziSira==true) {
                        hexButton.setStyle("-fx-background-color: red;");
                    } else {
                        hexButton.setStyle("-fx-background-color: blue;");
                    }
                    hexButton.setDisable(true);
                    applyFadeAnimationForRestart(hexButton);

                    if (!firstMoveMade) {
                        firstMoveMade = true;
                        firstMoveRow = finalRow;
                        firstMoveCol = finalCol;
                    }

                    kirmiziSira = !kirmiziSira;
                    updatePlayerButton();

                    if (checkForWinner(finalRow, finalCol)) {
                        showWinnerAlert(kirmiziSira ? "Mavi" : "Kırmızı");
                    }
                });

                gridPane.add(hexButton, col, row);
            }
        }
    } //Oyun tahtasını hexagonlarla oluşturduğumuz metod

    @FXML
    private Polygon createHexagon(double hexSize) {
        Polygon hexagon = new Polygon();
        double angle_step = Math.PI / 3;
        for (int i = 0; i < 6; i++) {
            double x = hexSize * Math.cos(i * angle_step);
            double y = hexSize * Math.sin(i * angle_step);
            hexagon.getPoints().addAll(x, y);
        }
        return hexagon;
    } //1 adet hexagon şekilinin oluşma kısmı

    @FXML
    private void restartGame() {
        createHexagonButtons(currentSize);
        kirmiziSira = true;
        firstMoveMade = false;
        updatePlayerButton();
    } //Oyunu yeniden başlatır.(Boyut seçtirtmez, önceden seçilen boyutta başlatır.)

    //------------------------------------------------------------------------------------------------------------------
        //Oynanış mekaniklerinin metodları

    @FXML //Oyuncunun belirli bir hat boyunca kaç adet taşının olduğunu sayar.
    private boolean checkDirection(int row, int col, String color, int rowInc, int colInc, int requiredCount) {
        int count = 1;
        int r = row + rowInc;
        int c = col + colInc;

        while (isValidPosition(r, c) && hexButtons[r][c].getStyle().contains(color)) {
            count++;
            r = r + rowInc;
            c = c + colInc;
        }

        r = row - rowInc;
        c = col - colInc;
        while (isValidPosition(r, c) && hexButtons[r][c].getStyle().contains(color)) {
            count++;
            r = r - rowInc;
            c = c - colInc;
        }

        return count >= requiredCount;
    }

    @FXML //Oyuncunun yaptığı hareketin geçerli bir satır ve sütunda olup olmadığını kontrol eder.
    private boolean isValidPosition(int row, int col) {
        Boolean duzgunMu;
        duzgunMu = ((row >= 0) && (row < hexButtons.length) && (col >= 0) && (col < hexButtons[0].length));
        return duzgunMu;
    }

    @FXML
    private boolean checkForWinner(int row, int col) {
        String hexColor = kirmiziSira ? "blue" : "red";
        int requiredCount = currentSize;
        Boolean kazandiMi;
        kazandiMi = checkDirection(row, col, hexColor, 0, 1, requiredCount) || checkDirection(row, col, hexColor, 1, 0, requiredCount) || checkDirection(row, col, hexColor, 1, 1, requiredCount) || checkDirection(row, col, hexColor, 1, -1, requiredCount);
        return kazandiMi;
    } //Oyuncu roundu kazandı mı onu belirleyen metod

    @FXML
    private void updatePlayerButton() {
        if (kirmiziSira == true) {
            playerButton.setStyle("-fx-background-color: RED;");
        } else {
            playerButton.setStyle("-fx-background-color: BLUE;");
        }
    } //Sıradaki oyuncunun kim olduğunu renkli olarak ekranda gösterir.

    //------------------------------------------------------------------------------------------------------------------
        //Butonlarla ilgili metodlar

    @FXML
    public void mainMenuButtonAction() {
        vboxStart.setVisible(true);
        mainVBox.setVisible(false);
        imageView.setVisible(true);
        initialize();
    } //Ana menüye döndüren buton

    @FXML
    public void startButtonAction() {
        RadioButton selectedRadioButton = (RadioButton) hex5x5.getToggleGroup().getSelectedToggle();
        if (selectedRadioButton != null) {
            int size = 5;
            if (selectedRadioButton == hex11x11) {
                size = 11;
            } else if (selectedRadioButton == hex17x17) {
                size = 17;
            }
            createHexagonButtons(size);
            vboxStart.setVisible(false);
            mainVBox.setVisible(true);
            imageView.setVisible(false);
            updateWinsLabels();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText(null);
            alert.setContentText("Lütfen bir boyut seçin!");
            alert.showAndWait();
        }
    } //Ana menüdeki oyunu başlatma butonu

    @FXML
    public void swapButtonAction() {
        if (swapOptionAvailable == true) {
            kirmiziSira = !kirmiziSira; // Sıradaki oyuncuyu kırmızıysa mavi, maviyse kırmızı olarak değiştirir
            updatePlayerButton(); // Oyuncu butonunu güncelle
            applyFadeAnimationForSwap(swapButton);
            swapOptionAvailable = false; // Her round 1 kez hak olduğu için false olarak bitirir.
        }


    } //Swap kuralının butonu (Her round 1 kez çalışır, sonra yenilenir)

    @FXML
    public void restartButtonAction() {
        applyFadeAnimationForRestart(restartButton);
        createHexagonButtons(currentSize);
        kirmiziSira = true;
        firstMoveMade = false;
        updatePlayerButton();
    } //Oyunu yeniden başlatma butonu

    //------------------------------------------------------------------------------------------------------------------
        //Oyun kazanma bildirim metodları

    @FXML
    private void showWinnerAlert(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Oyun Bitti");
        alert.setHeaderText(null);
        alert.setContentText(winner + " oyuncu kazandı!");
        alert.showAndWait();

        if (winner.equals("Kırmızı")) {
            redWins++;
        } else {
            blueWins++;
        }
        restartGame();
        updateWinsLabels();
        removeFadeAnimationForButtons(swapButton); // Swap butonu için parkalığın eski haline dönmesi
        swapOptionAvailable = true; // Round bitince swap hakkının geri gelmesi için
        removeFadeAnimationForButtons(restartButton); //Restart butonu için parkalığın eski haline dönmesi
        removeFadeAnimationForButtons(mainMenuButton); //Ana menü butonu için parkalığın eski haline dönmesi
    } //Round'u kazananın kim olduğunun bildirimi + butonların animasyonu

    @FXML
    private void updateWinsLabels() {
        redWinsLabel.setText("I. Oyuncu: " + redWins);
        blueWinsLabel.setText("II. Oyuncu: " + blueWins);
    } // Skor tablosu

    //------------------------------------------------------------------------------------------------------------------
        //Animasyon metodları

    @FXML
    private void applyFadeAnimationForRestart(Button hexButton) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(450), hexButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.1);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }//Restart butonunun opaklığını düşürüp geri eski haline getirir.


    @FXML
    private void applyFadeAnimationForSwap(Button hexButton) { //Bu animasyonda sadece opaklığı düşürür öyle kalır.
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(450), hexButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.1);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();
    }//Swap butonunun opaklığını düşürüp sabit bırakır.


    @FXML
    private void removeFadeAnimationForButtons(Button hexButton) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), hexButton);
        fadeTransition.setFromValue(0.1);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();
    }//Herhangi bir butonun opaklığını eski haline geri getirir.

    //------------------------------------------------------------------------------------------------------------------
}
