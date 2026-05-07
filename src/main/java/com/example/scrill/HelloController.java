package com.example.scrill;

import com.example.scrill.controller.*;
import com.example.scrill.util.StatsManager;
import com.example.scrill.util.ThemeManager;
import com.example.scrill.ui.DashboardBuilder;
import com.example.scrill.ui.ModalManager;
import com.example.scrill.ui.SidebarController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.CacheHint;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class HelloController {

    // ==========================================
    // FXML ПОЛЯ
    // ==========================================
    @FXML public StackPane nowPlayingWidget;
    @FXML public VBox nowPlayingContent;
    @FXML private ScrollPane jumpBackInScroll;
    @FXML public ComboBox<String> trackSizeCombo;
    @FXML public CheckBox ignoreClipToggle;
    @FXML public StackPane deleteConfirmOverlay;
    @FXML public VBox deleteConfirmCard;
    @FXML public Label deleteConfirmTitle;
    @FXML private ImageView onboardingIcon;
    @FXML public VBox weeklyActivityContainer;
    @FXML public Label addPlaylistBtn;
    @FXML private Label addTrackLabel;
    @FXML public TextField searchField;
    @FXML public StackPane titleViewport;
    @FXML public Slider progressSlider;
    @FXML public Label currentTimeLabel;
    @FXML public Label totalTimeLabel;
    @FXML public FontIcon playIcon;
    @FXML public ImageView coverImage;
    @FXML public Label trackTitleLabel;
    @FXML public Label artistNameLabel;
    @FXML public FontIcon repeatIcon;
    @FXML public FontIcon shuffleIcon;
    @FXML public Slider volumeSlider;
    @FXML private FontIcon volumeIcon;
    @FXML private FontIcon clearSearchIcon;
    @FXML public TableView<Track> trackTable;
    @FXML public TableColumn<Track, Integer> idCol;
    @FXML public TableColumn<Track, Track> coverCol;
    @FXML public TableColumn<Track, Track> infoCol;
    @FXML public TableColumn<Track, String> dateCol;
    @FXML public TableColumn<Track, String> durationCol;
    @FXML public Label libraryStatsLabel;
    @FXML private ImageView minimizeBtn;
    @FXML private ImageView maximizeBtn;
    @FXML private ImageView closeBtn;
    @FXML private StackPane mainContentArea;
    @FXML public StackPane addSongFAB;
    @FXML public StackPane modalOverlay;
    @FXML public VBox modalCard;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button mainPlayBtn;
    @FXML private Rectangle fabBackground;
    @FXML public ImageView modalCloseBtn;
    @FXML public ImageView newTrackCoverImage;
    @FXML public TextField newTrackTitleField;
    @FXML public TextField newTrackArtistField;
    @FXML public Label selectedAudioLabel;
    @FXML public Button tabManualBtn;
    @FXML public Button tabLinkBtn;
    @FXML public VBox manualAddForm;
    @FXML public VBox linkAddForm;
    @FXML public Label manualAddErrorLabel;
    @FXML public StackPane coverUploadArea;
    @FXML public Button downloadLinkBtn;
    @FXML public TextField newTrackLinkField;
    @FXML public StackPane profileBtn;
    @FXML public StackPane profileModalOverlay;
    @FXML public VBox profileModalCard;
    @FXML public TextField newProfileNameField;
    @FXML public ImageView profileModalCloseBtn;
    @FXML public VBox sidebar;
    @FXML public HBox navHomeBtn;
    @FXML public HBox navLibraryBtn;
    @FXML public Label playlistsHeaderLabel;
    @FXML public VBox playlistsContainer;
    @FXML public StackPane playlistModalOverlay;
    @FXML public VBox playlistModalCard;
    @FXML public TextField newPlaylistNameField;
    @FXML public ImageView newPlaylistCoverImage;
    @FXML public ImageView playlistModalCloseBtn;
    @FXML public Region backIcon, forwardIcon, settingsIcon;
    @FXML public Label mainTitleLabel;
    @FXML public Label greetingLabel;
    @FXML public ImageView mainHeaderCover;
    @FXML public ScrollPane homeDashboardScroll;
    @FXML public Label homeTotalTracks;
    @FXML public Label homeTotalDuration;
    @FXML public Label homeTotalPlaylists;
    @FXML public HBox recentlyAddedContainer;
    @FXML public HBox searchContainer;
    @FXML public Label mainSubtitleLabel;
    @FXML public Button savePlaylistBtn;
    @FXML public Label playlistModalTitle;
    @FXML public StackPane loadingOverlay;
    @FXML public StackPane settingsModalOverlay;
    @FXML public VBox settingsModalCard;
    @FXML public CheckBox animationsToggle;
    @FXML public ImageView settingsModalCloseBtn;
    @FXML public CheckBox autoPlayToggle;
    @FXML public ComboBox<String> startScreenCombo;
    @FXML public StackPane rootPane;
    @FXML public ComboBox<String> themeCombo;
    @FXML public Label homeTotalListened;
    @FXML public Label homeWeeklyListened;
    @FXML public FlowPane topArtistsContainer;
    @FXML public FlowPane homePlaylistsContainer;
    @FXML public VBox heavyRotationContainer;
    @FXML public VBox dailyGoalContainer;
    @FXML public VBox streakContainer;
    @FXML public VBox paletteContainer;
    @FXML public Button tabPlaylistManualBtn;
    @FXML public Button tabPlaylistLinkBtn;
    @FXML public VBox playlistManualForm;
    @FXML public VBox playlistLinkForm;
    @FXML public TextField importPlaylistLinkField;
    @FXML public Button importPlaylistBtn;
    @FXML public Label importProgressLabel;
    @FXML public StackPane editTrackModalOverlay;
    @FXML public VBox editTrackModalCard;
    @FXML public TextField editTrackTitleField;
    @FXML public TextField editTrackArtistField;
    @FXML public ImageView editTrackCoverImage;
    @FXML public HBox playlistTabHeader;
    @FXML public StackPane playlistCoverArea;
    @FXML public StackPane darkModeToggle;
    @FXML private StackPane onboardingOverlay;
    @FXML public TextField onboardingNameField;
    @FXML public Label profileNameLabel;
    @FXML public FontIcon addSongIcon;
    @FXML private FontIcon queueIcon;
    @FXML public VBox dashboardMainVBox;
    @FXML public HBox dashboardFirstRow;
    @FXML public VBox mostPlayedContainer;

    // ==========================================
    // SUB-CONTROLLERS
    // ==========================================
    public LibraryController libraryController;
    public SidebarController sidebarController;
    public ModalManager modalManager;
    public DashboardBuilder dashboardBuilder;
    public NavigationController navigationController;
    public PlayerController playerController;
    public ProfileController profileController;
    public SettingsController settingsController;

    // ==========================================
    // СОСТОЯНИЕ ПРИЛОЖЕНИЯ
    // ==========================================
    public final String ROOT_MUSIC_DIR =
            System.getProperty("user.home") + "/AppData/Roaming/Scrill/music";
    public Track globalPlayingTrack;
    public String currentView = "HOME";
    public String activePlaylist = null;
    public boolean isQueueMode = false;
    public List<Track> playQueue = new ArrayList<>();

    // Алиасы на поля libraryController (для удобства доступа из других классов)
    public javafx.collections.ObservableList<Track> trackListData;
    public List<File> musicFiles;
    public Map<String, String> durationCache;

    // Toggle состояние (только для UI — сам тоггл в setupThemeToggle)
    public javafx.scene.shape.Rectangle toggleBg;
    public javafx.scene.shape.Circle toggleThumb;
    public boolean tempDarkModeState = true;

    // Приватные UI поля
    private boolean isDashboardExpanded = false;
    private List<Track> currentHeavyRotation = new ArrayList<>();
    private boolean isWindowDragging = false;
    private Timeline fabTimeline = new Timeline();
    private Image maxiIcon;
    private Image miniIcon;
    private double xOffset = 0;
    private double yOffset = 0;
    private Timeline listeningTimer;


    public Stack<String> backHistory = new Stack<>();
    public Stack<String> forwardHistory = new Stack<>();

    // ==========================================
    // ГЕТТЕРЫ ДЛЯ SUB-CONTROLLERS
    // ==========================================
    public javafx.collections.ObservableList<Track> getTrackListData() { return trackListData; }
    public Track getGlobalPlayingTrack() { return globalPlayingTrack; }
    public boolean isPlaying() { return playerController != null && playerController.isPlaying; }
    public String getCurrentView() { return currentView; }
    public void setCurrentView(String view) { this.currentView = view; }
    public String getCurrentProfileName() {
        return profileController != null ? profileController.currentProfileName : null;
    }
    public List<File> getMusicFiles() { return musicFiles; }
    public List<Track> getCurrentHeavyRotation() { return currentHeavyRotation; }
    public void setIsDashboardExpanded(boolean val) { isDashboardExpanded = val; }
    public boolean getIsDashboardExpanded() { return isDashboardExpanded; }
    public File getCurrentMusicFolder() {
        return profileController != null
                ? profileController.getCurrentMusicFolder()
                : new File(ROOT_MUSIC_DIR, "temp_guest");
    }

    // ==========================================
    // INITIALIZE
    // ==========================================
    @FXML
    public void initialize() {
        // Инициализируем все sub-controllers
        dashboardBuilder  = new DashboardBuilder(this);
        modalManager      = new ModalManager(this);
        sidebarController = new SidebarController(this);
        libraryController = new LibraryController(this);
        navigationController = new NavigationController(this);
        playerController  = new PlayerController(this);
        profileController = new ProfileController(this);
        settingsController = new SettingsController(this);

        playerController.initialize();

        // Алиасы
        trackListData = libraryController.trackListData;
        musicFiles    = libraryController.musicFiles;
        durationCache = libraryController.durationCache;

        // Обложка (нижняя панель)
        coverImage.setSmooth(true);
        coverImage.setCache(true);
        coverImage.setCacheHint(CacheHint.QUALITY);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(coverImage.fitWidthProperty());
        clip.heightProperty().bind(coverImage.fitHeightProperty());
        clip.setArcWidth(20); clip.setArcHeight(20);
        coverImage.setClip(clip);

        setupThemeToggle();

        // Обложка редактирования трека
        Rectangle editCoverClip = new Rectangle(126, 126);
        editCoverClip.setArcWidth(15); editCoverClip.setArcHeight(15);
        editTrackCoverImage.setClip(editCoverClip);
        setupObjectFitCover(editTrackCoverImage);

        // Конфиг и комбобоксы
        settingsController.loadAppConfig();
        startScreenCombo.getItems().addAll("Home", "Library");
        if (themeCombo != null)
            themeCombo.getItems().addAll("Default", "Red", "Green", "Cream", "Pink", "Nordic", "Penguin");
        if (trackSizeCombo != null)
            trackSizeCombo.getItems().addAll("Compact", "Default", "Large");

        // Обложки
        setupObjectFitCover(coverImage);
        setupObjectFitCover(mainHeaderCover);
        setupObjectFitCover(newPlaylistCoverImage);
        setupObjectFitCover(newTrackCoverImage);

        Rectangle newCoverClip = new Rectangle(126, 126);
        newCoverClip.setArcWidth(15); newCoverClip.setArcHeight(15);
        newTrackCoverImage.setClip(newCoverClip);

        artistNameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        trackTitleLabel.setTextOverrun(OverrunStyle.CLIP);
        trackTitleLabel.setMinWidth(Region.USE_PREF_SIZE);

        Rectangle playlistClip = new Rectangle(96, 96);
        playlistClip.setArcWidth(15); playlistClip.setArcHeight(15);
        newPlaylistCoverImage.setClip(playlistClip);

        Rectangle headerClip = new Rectangle(80, 80);
        headerClip.setArcWidth(20); headerClip.setArcHeight(20);
        mainHeaderCover.setClip(headerClip);

        Rectangle playlistClipBinding = new Rectangle();
        playlistClipBinding.widthProperty().bind(newPlaylistCoverImage.fitWidthProperty());
        playlistClipBinding.heightProperty().bind(newPlaylistCoverImage.fitHeightProperty());
        playlistClipBinding.setArcWidth(25); playlistClipBinding.setArcHeight(25);
        newPlaylistCoverImage.setClip(playlistClipBinding);
        newPlaylistCoverImage.fitWidthProperty().bind(playlistCoverArea.prefWidthProperty().subtract(10));
        newPlaylistCoverImage.fitHeightProperty().bind(playlistCoverArea.prefHeightProperty().subtract(10));
        setupObjectFitCover(newPlaylistCoverImage);

        // Иконки навигации
        if (settingsIcon != null) {
            setupVectorIconEffects(settingsIcon);
            settingsIcon.setOnMouseClicked(e -> settingsController.openSettingsModal());
        }
        if (backIcon != null) setupVectorIconEffects(backIcon);
        if (forwardIcon != null) setupVectorIconEffects(forwardIcon);

        // Иконка очереди
        if (queueIcon != null) {
            queueIcon.setOnMouseEntered(e -> {
                javafx.scene.effect.ColorAdjust makeWhite = new javafx.scene.effect.ColorAdjust();
                makeWhite.setBrightness(1.0);
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow(
                        10, Color.rgb(255, 255, 255, 0.4));
                glow.setInput(makeWhite);
                queueIcon.setEffect(glow);
                queueIcon.setCursor(javafx.scene.Cursor.HAND);
            });
            queueIcon.setOnMouseExited(e -> updateQueueIndicator());
        }

        // Кнопки плеера
        playerController.setupMediaButtonEffects(prevBtn);
        playerController.setupMediaButtonEffects(mainPlayBtn);
        playerController.setupMediaButtonEffects(nextBtn);

        // Слайдер прогресса
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            playerController.updateSliderGradient();
            if (playerController.isSliderDragging) {
                currentTimeLabel.setText(playerController.formatTime(
                        Duration.seconds(newVal.doubleValue())));
            }
        });

        // Слайдер громкости
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            if (playerController.mediaPlayer != null)
                playerController.mediaPlayer.setVolume(Math.pow(vol, 2));
            playerController.updateVolumeGradient();
            if (vol <= 0.01) volumeIcon.setIconLiteral("mdi-volume-off");
            else if (vol < 0.5) volumeIcon.setIconLiteral("mdi-volume-medium");
            else volumeIcon.setIconLiteral("mdi-volume-high");
        });

        volumeIcon.setOnMouseClicked(e -> playerController.onVolumeClick());
        volumeIcon.setOnMouseEntered(e -> {
            volumeIcon.setEffect(new javafx.scene.effect.DropShadow(
                    10, Color.rgb(255, 255, 255, 0.4)));
            volumeIcon.setCursor(javafx.scene.Cursor.HAND);
        });
        volumeIcon.setOnMouseExited(e -> volumeIcon.setEffect(null));

        // Блок настройки сцены и таблицы
        Platform.runLater(() -> {
            if (trackTitleLabel.getScene() == null) return;

            settingsController.loadAppConfig();
            applyTheme(ThemeManager.getInstance().getCurrentBaseTheme(),
                    ThemeManager.getInstance().isDarkMode());

            javafx.scene.Scene scene = trackTitleLabel.getScene();
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();

            scene.setFill(ThemeManager.getInstance().isDarkMode()
                    ? Color.web("#121212") : Color.web("#F4F4F9"));

            // Маска для marquee
            javafx.scene.shape.Rectangle clipp = new javafx.scene.shape.Rectangle();
            clipp.widthProperty().bind(titleViewport.widthProperty().subtract(4));
            clipp.heightProperty().bind(titleViewport.heightProperty());
            titleViewport.setClip(clipp);

            playerController.updateVolumeGradient();
            playerController.updateSliderGradient();

            // Горячие клавиши
            scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                if (scene.getFocusOwner() instanceof TextInputControl) return;
                switch (event.getCode()) {
                    case SPACE -> { playerController.onPlayClick(); event.consume(); }
                    case RIGHT -> { playerController.seekForward(); event.consume(); }
                    case LEFT  -> { playerController.seekBackward(); event.consume(); }
                    case UP    -> { volumeSlider.setValue(Math.min(volumeSlider.getValue() + 0.1, 1.0)); event.consume(); }
                    case DOWN  -> { volumeSlider.setValue(Math.max(volumeSlider.getValue() - 0.1, 0.0)); event.consume(); }
                    case M     -> { playerController.onVolumeClick(); event.consume(); }
                    case ESCAPE -> {
                        if (searchField.isFocused()) { trackTable.requestFocus(); event.consume(); }
                    }
                }
            });

            // Drag & Drop MP3
            scene.setOnDragOver(event -> {
                if (event.getDragboard().hasFiles()) {
                    boolean hasMP3 = event.getDragboard().getFiles().stream()
                            .anyMatch(f -> f.getName().toLowerCase().endsWith(".mp3"));
                    if (hasMP3) {
                        event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                        trackTable.setStyle(
                                "-fx-border-color: -accent-purple; -fx-border-width: 2; -fx-border-radius: 10;");
                    }
                }
                event.consume();
            });
            scene.setOnDragExited(event -> { trackTable.setStyle(""); event.consume(); });
            scene.setOnDragDropped(event -> {
                javafx.scene.input.Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    List<File> mp3Files = db.getFiles().stream()
                            .filter(f -> f.getName().toLowerCase().endsWith(".mp3"))
                            .collect(Collectors.toList());
                    if (!mp3Files.isEmpty()) {
                        success = true;
                        trackTable.setStyle("");
                        libraryController.handleDroppedFiles(mp3Files);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // Снятие фокуса с поиска
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (searchField.isFocused()
                        && !searchField.getBoundsInParent().contains(event.getX(), event.getY()))
                    trackTable.requestFocus();
            });

            titleViewport.widthProperty().addListener((obs, oldVal, newVal) ->
                    playerController.updateMarquee());

            // Настройка таблицы
            idCol.setReorderable(false); coverCol.setReorderable(false);
            infoCol.setReorderable(false); dateCol.setReorderable(false);
            durationCol.setReorderable(false);

            idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateAdded"));
            durationCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("duration"));
            coverCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue()));
            infoCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue()));

            Label placeholder = new Label("Nothing has been found");
            placeholder.setStyle("-fx-text-fill: #ADA9BA; -fx-font-size: 16px; -fx-font-weight: bold;");
            trackTable.setPlaceholder(placeholder);

            clearSearchIcon.visibleProperty().bind(searchField.textProperty().isNotEmpty());
            clearSearchIcon.setOnMouseClicked(e -> searchField.clear());

            javafx.collections.transformation.FilteredList<Track> filteredData =
                    new javafx.collections.transformation.FilteredList<>(trackListData, p -> true);
            searchField.textProperty().addListener((obs, oldVal, newVal) ->
                    filteredData.setPredicate(track -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String filter = newVal.toLowerCase();
                        return track.getTitle().toLowerCase().contains(filter)
                                || track.getArtist().toLowerCase().contains(filter);
                    }));

            javafx.collections.transformation.SortedList<Track> sortedData =
                    new javafx.collections.transformation.SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(trackTable.comparatorProperty());
            trackTable.setItems(sortedData);

            libraryController.applyTrackDisplaySize();

            // Двойной клик по треку
            trackTable.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !trackTable.getSelectionModel().isEmpty()) {
                    Track selected = trackTable.getSelectionModel().getSelectedItem();
                    playerController.currentTrackIndex = musicFiles.indexOf(selected.getFile());
                    playerController.isPlaying = true;
                    playerController.prepareTrack(selected.getFile());
                    playerController.updateUI(selected);
                    playIcon.setIconLiteral("mdi-pause-circle");
                    playerController.smoothProgressTimer.start();
                    if (playerController.coverPulse != null) playerController.coverPulse.play();
                }
            });

            // Иконки окна
            try {
                maxiIcon = new Image(getClass().getResourceAsStream("Icons/maxi.png"));
                miniIcon = new Image(getClass().getResourceAsStream("Icons/mini.png"));
            } catch (Exception e) {
                System.err.println("Ошибка загрузки оконных иконок: " + e.getMessage());
            }

            setupWindowBtn(minimizeBtn);
            setupWindowBtn(maximizeBtn);
            setupWindowBtn(closeBtn);
            preventDoubleClickMaximize(minimizeBtn);
            preventDoubleClickMaximize(maximizeBtn);
            preventDoubleClickMaximize(closeBtn);

            if (settingsIcon != null) {
                preventDoubleClickMaximize(settingsIcon);
                if (settingsIcon.getParent() != null)
                    preventDoubleClickMaximize(settingsIcon.getParent());
            }
            if (backIcon != null) {
                preventDoubleClickMaximize(backIcon);
                if (backIcon.getParent() != null)
                    preventDoubleClickMaximize(backIcon.getParent());
            }
            if (forwardIcon != null) preventDoubleClickMaximize(forwardIcon);

            setupWindowBtn(settingsModalCloseBtn);
            setupModalCloseBtn(modalCloseBtn);
            setupModalCloseBtn(profileModalCloseBtn);
            setupModalCloseBtn(playlistModalCloseBtn);
            setupModalCloseBtn(settingsModalCloseBtn);

            minimizeBtn.setOnMouseClicked(e -> stage.setIconified(true));
            maximizeBtn.setOnMouseClicked(e -> {
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    maximizeBtn.setImage(maxiIcon);
                } else {
                    javafx.geometry.Rectangle2D bounds =
                            javafx.stage.Screen.getPrimary().getVisualBounds();
                    stage.setX(bounds.getMinX());
                    stage.setY(bounds.getMinY());
                    stage.setWidth(bounds.getWidth());
                    stage.setHeight(bounds.getHeight());
                    stage.setMaximized(true);
                    maximizeBtn.setImage(miniIcon);
                }
            });
            closeBtn.setOnMouseClicked(e -> {
                listeningTimer.stop();
                if (playerController.smoothProgressTimer != null)
                    playerController.smoothProgressTimer.stop();
                StatsManager.getInstance().saveListeningStats();

                //System.out.println("[Close] globalPlayingTrack = " +
                        //(globalPlayingTrack != null ? globalPlayingTrack.getFile().getName() : "NULL"));

                settingsController.saveAppConfig();
                Platform.exit();
            });

            stage.setOnCloseRequest(e -> {
                listeningTimer.stop();
                if (playerController.smoothProgressTimer != null)
                    playerController.smoothProgressTimer.stop();
                StatsManager.getInstance().saveListeningStats();
                settingsController.saveAppConfig();
                Platform.exit();
            });

            // Перетаскивание окна
            scene.setOnMousePressed(event -> {
                if (event.getSceneY() < 70
                        && event.getSceneX() < scene.getWidth() - 250
                        && !stage.isMaximized()) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                    isWindowDragging = true;
                }
            });
            scene.setOnMouseDragged(event -> {
                if (isWindowDragging && !stage.isMaximized()) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            scene.setOnMouseReleased(event -> isWindowDragging = false);
            scene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2
                        && event.getSceneY() < 70
                        && event.getSceneX() < scene.getWidth() - 250) {
                    if (stage.isMaximized()) {
                        stage.setMaximized(false);
                        maximizeBtn.setImage(maxiIcon);
                    } else {
                        javafx.geometry.Rectangle2D bounds =
                                javafx.stage.Screen.getPrimary().getVisualBounds();
                        stage.setX(bounds.getMinX());
                        stage.setY(bounds.getMinY());
                        stage.setWidth(bounds.getWidth());
                        stage.setHeight(bounds.getHeight());
                        stage.setMaximized(true);
                        maximizeBtn.setImage(miniIcon);
                    }
                }
            });

            for (Node node : rootPane.lookupAll(".stat-card")) setupSmoothCardHover(node);
            for (Node node : rootPane.lookupAll(".hero-card")) setupHeroCardAnimation(node);
        });

        setupTableContextMenu();

        fabBackground.widthProperty().bind(addSongFAB.prefWidthProperty());
        addSongFAB.setOnMouseEntered(e -> {
            addTrackLabel.setVisible(true);
            fabTimeline.stop();
            fabTimeline = new Timeline(new KeyFrame(Duration.millis(250),
                    new KeyValue(addSongFAB.prefWidthProperty(), 150.0, Interpolator.EASE_BOTH),
                    new KeyValue(addSongFAB.minWidthProperty(), 150.0, Interpolator.EASE_BOTH),
                    new KeyValue(addSongFAB.maxWidthProperty(), 150.0, Interpolator.EASE_BOTH),
                    new KeyValue(addTrackLabel.opacityProperty(), 1.0, Interpolator.EASE_BOTH)));
            fabTimeline.play();
        });
        addSongFAB.setOnMouseExited(e -> {
            fabTimeline.stop();
            fabTimeline = new Timeline(new KeyFrame(Duration.millis(200),
                    new KeyValue(addSongFAB.prefWidthProperty(), 56.0, Interpolator.EASE_BOTH),
                    new KeyValue(addSongFAB.minWidthProperty(), 56.0, Interpolator.EASE_BOTH),
                    new KeyValue(addSongFAB.maxWidthProperty(), 56.0, Interpolator.EASE_BOTH),
                    new KeyValue(addTrackLabel.opacityProperty(), 0.0, Interpolator.EASE_BOTH)));
            fabTimeline.setOnFinished(event -> {
                if (!addSongFAB.isHover()) addTrackLabel.setVisible(false);
            });
            fabTimeline.play();
        });

        // Таймер прослушивания
        listeningTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (playerController.isPlaying && playerController.mediaPlayer != null) {
                StatsManager.getInstance().incrementListeningTime();
                if (StatsManager.getInstance().getTotalListeningSeconds() % 10 == 0) {
                    StatsManager.getInstance().saveListeningStats();
                }
                if ("Home".equals(currentView)) updateListeningUI();
            }
        }));
        listeningTimer.setCycleCount(Animation.INDEFINITE);
        listeningTimer.play();

        setupWindowBtnProfile(profileBtn);
        sidebarController.setupAdaptiveSidebar();

        newTrackTitleField.textProperty().addListener((obs, oldVal, newVal) -> {
            newTrackTitleField.getStyleClass().remove("input-error");
            manualAddErrorLabel.setVisible(false);
        });
        newTrackArtistField.textProperty().addListener((obs, oldVal, newVal) -> {
            newTrackArtistField.getStyleClass().remove("input-error");
            manualAddErrorLabel.setVisible(false);
        });

        // Адаптивный виджет Now Playing
        homeDashboardScroll.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (dashboardFirstRow == null || dashboardMainVBox == null
                    || nowPlayingWidget == null) return;
            double currentWidth = newVal.doubleValue();
            boolean isWide = currentWidth >= 950;
            if (isWide && !dashboardFirstRow.getChildren().contains(nowPlayingWidget)) {
                dashboardMainVBox.getChildren().remove(nowPlayingWidget);
                dashboardFirstRow.getChildren().add(nowPlayingWidget);
                nowPlayingWidget.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nowPlayingWidget, Priority.ALWAYS);
            } else if (!isWide && dashboardFirstRow.getChildren().contains(nowPlayingWidget)) {
                dashboardFirstRow.getChildren().remove(nowPlayingWidget);
                int insertIndex = dashboardMainVBox.getChildren().indexOf(dashboardFirstRow) + 1;
                dashboardMainVBox.getChildren().add(insertIndex, nowPlayingWidget);
                nowPlayingWidget.setMaxWidth(Double.MAX_VALUE);
            }
        });

        // Адаптивный дашборд
        topArtistsContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if ("Home".equals(currentView)) {
                boolean shouldBeExpanded = newVal.doubleValue() >= 430;
                if (shouldBeExpanded != isDashboardExpanded) {
                    isDashboardExpanded = shouldBeExpanded;
                    dashboardBuilder.updateTopArtistsUI(isDashboardExpanded ? 6 : 4);
                    dashboardBuilder.updateHeavyRotationUI(isDashboardExpanded ? 6 : 4);
                }
            }
        });

        // Jump Back In горизонтальный скролл
        if (jumpBackInScroll != null) {
            jumpBackInScroll.setMinHeight(230);
            jumpBackInScroll.setPrefHeight(230);
            jumpBackInScroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
                if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                    double step = event.getDeltaY() / 600.0;
                    double newValue = jumpBackInScroll.getHvalue() - step;
                    jumpBackInScroll.setHvalue(Math.max(0.0, Math.min(1.0, newValue)));
                    event.consume();
                }
            });
        }

        Platform.runLater(this::startApplicationSequence);
    }

    // ==========================================
    // ЗАПУСК ПРИЛОЖЕНИЯ
    // ==========================================

    public void startApplicationSequence() {
        loadingOverlay.setOpacity(1.0);
        loadingOverlay.setScaleX(1.0);
        loadingOverlay.setScaleY(1.0);
        onboardingOverlay.setOpacity(1.0);
        loadingOverlay.setVisible(true);
        onboardingOverlay.setVisible(false);

        settingsController.loadAppConfig();
        applyTheme(ThemeManager.getInstance().getCurrentBaseTheme(),
                ThemeManager.getInstance().isDarkMode());

        File profileDir = new File(ROOT_MUSIC_DIR);
        if (!profileDir.exists()) profileDir.mkdirs();

        File[] profiles = profileDir.listFiles(File::isDirectory);
        boolean hasProfiles = (profiles != null && profiles.length > 0);

        if (!hasProfiles) {
            loadingOverlay.setVisible(false);
            onboardingOverlay.setVisible(true);
            onboardingOverlay.toFront();
            modalManager.toggleGlassmorphism(true);
            mainPlayBtn.setDisable(true);
        } else {
            String savedProfile = profileController.currentProfileName;
            if (savedProfile == null || !new File(ROOT_MUSIC_DIR, savedProfile).exists()) {
                profileController.currentProfileName = profiles[0].getName();
                profileController.profileFolder = profiles[0];
                settingsController.saveAppConfig();
            } else {
                profileController.profileFolder = new File(ROOT_MUSIC_DIR, savedProfile);
            }
            StatsManager.getInstance().setProfileFolder(profileController.profileFolder);
            continueNormalStartup();
        }
    }

    public void continueNormalStartup() {
        profileNameLabel.setText(profileController.currentProfileName);
        libraryController.loadDurationCache();
        StatsManager.getInstance().setProfileFolder(getCurrentMusicFolder());
        StatsManager.getInstance().loadPlayCounts();
        StatsManager.getInstance().loadListeningStats();
        sidebarController.loadPlaylistsUI();

        Platform.runLater(this::updateListeningUI);

        new Thread(() -> {
            File folder = getCurrentMusicFolder();
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

            Platform.runLater(() -> {
                if (files != null) {
                    Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
                    musicFiles.clear();
                    trackListData.clear();
                    int index = 1;
                    for (File file : files) {
                        String fileName = file.getName().replace(".mp3", "");
                        String title = fileName, artist = "Unknown Artist";
                        if (fileName.contains("~~")) {
                            String[] parts = fileName.split("~~", 2);
                            title = parts[0].trim();
                            artist = parts[1].trim();
                        }
                        String date = new java.text.SimpleDateFormat("dd.MM.yyyy")
                                .format(file.lastModified());
                        String duration = durationCache.getOrDefault(file.getName(), "0:00");
                        Image cover = libraryController.loadSmallCover(file, fileName);
                        trackListData.add(new Track(index++, title, artist, date, duration, cover, file));
                        musicFiles.add(file);
                    }
                }


                if (!musicFiles.isEmpty()) {
                    File trackToLoad = musicFiles.get(0);
                    if (settingsController.lastPlayedFileName != null) {
                        for (File f : musicFiles) {
                            if (f.getName().equals(settingsController.lastPlayedFileName)) {
                                trackToLoad = f;
                                break;
                            }
                        }
                    }
                    playerController.prepareTrack(trackToLoad);

                }

                navigateTo(settingsController.defaultStartScreen, false);


                PauseTransition settlePause = new PauseTransition(Duration.millis(300));
                settlePause.setOnFinished(ev -> {
                    loadingOverlay.setMouseTransparent(true);
                    Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(500),
                            new KeyValue(loadingOverlay.opacityProperty(), 0.0, Interpolator.EASE_BOTH)));
                    fadeOut.setOnFinished(e -> {
                        loadingOverlay.setVisible(false);
                        libraryController.calculateDurations();
                    });
                    fadeOut.play();
                });
                settlePause.play();
            });
        }).start();
    }

    // ==========================================
    // ТЕМА
    // ==========================================

    public void applyTheme(String baseTheme, boolean isDark) {
        if (rootPane == null) return;
        ThemeManager.getInstance().setCurrentBaseTheme(baseTheme);
        ThemeManager.getInstance().setDarkMode(isDark);

        double iconBrightness = isDark ? 1.0 : -1.0;

        if (rootPane.getScene() != null) {
            rootPane.getScene().setFill(isDark
                    ? Color.web("#121212") : Color.web("#F4F4F9"));
        }

        ThemeManager.getInstance().updateImageBrightness(minimizeBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(maximizeBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(closeBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(modalCloseBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(profileModalCloseBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(playlistModalCloseBtn, iconBrightness);
        ThemeManager.getInstance().updateImageBrightness(settingsModalCloseBtn, iconBrightness);

        rootPane.getStyleClass().removeAll(
                "light-theme", "red-theme", "light-red-theme",
                "green-theme", "light-green-theme", "cream-theme",
                "dark-cream-theme", "pink-theme", "light-pink-theme",
                "nordic-frost", "light-nordic", "penguin-theme", "light-penguin");

        switch (baseTheme) {
            case "Default" -> { if (!isDark) rootPane.getStyleClass().add("light-theme"); }
            case "Red"     -> rootPane.getStyleClass().add(isDark ? "red-theme" : "light-red-theme");
            case "Green"   -> rootPane.getStyleClass().add(isDark ? "green-theme" : "light-green-theme");
            case "Cream"   -> rootPane.getStyleClass().add(isDark ? "dark-cream-theme" : "cream-theme");
            case "Pink"    -> rootPane.getStyleClass().add(isDark ? "pink-theme" : "light-pink-theme");
            case "Nordic"  -> rootPane.getStyleClass().add(isDark ? "nordic-frost" : "light-nordic");
            case "Penguin" -> rootPane.getStyleClass().add(isDark ? "penguin-theme" : "light-penguin");
        }

        if (darkModeToggle != null && toggleBg != null) {
            ThemeManager.getInstance().animateToggle(isDark);
        }

        if (playerController != null) {
            playerController.updateVolumeGradient();
            playerController.updateSliderGradient();
        }

        updateOnboardingIconColor(isDark);

        if (addSongFAB != null) {
            addSongFAB.setStyle(baseTheme.equals("Penguin") && isDark
                    ? "-fab-color: black;" : "-fab-color: white;");
        }
    }

    private void updateOnboardingIconColor(boolean isDark) {
        if (onboardingIcon == null) return;
        String activeHex = ThemeManager.getInstance().getAccentHex();
        javafx.scene.effect.ColorInput colorInput = new javafx.scene.effect.ColorInput(
                0, 0, 60, 60, Color.web(activeHex));
        javafx.scene.effect.Blend tint = new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_ATOP);
        tint.setTopInput(colorInput);
        onboardingIcon.setEffect(tint);
    }

    private void setupThemeToggle() {
        if (darkModeToggle == null) return;

        toggleBg = new javafx.scene.shape.Rectangle(44, 24);
        toggleBg.setArcWidth(24); toggleBg.setArcHeight(24);
        toggleBg.setFill(Color.web("#706F8E"));

        toggleThumb = new javafx.scene.shape.Circle(9);
        toggleThumb.setFill(Color.WHITE);
        toggleThumb.setTranslateX(-10);
        toggleThumb.setEffect(new javafx.scene.effect.DropShadow(5, Color.rgb(0, 0, 0, 0.3)));

        darkModeToggle.getChildren().addAll(toggleBg, toggleThumb);
        ThemeManager.getInstance().setToggleControls(toggleBg, toggleThumb);
        darkModeToggle.setCursor(javafx.scene.Cursor.HAND);

        darkModeToggle.setOnMouseClicked(e -> {
            tempDarkModeState = !tempDarkModeState;
            ThemeManager.getInstance().setTempDarkModeState(tempDarkModeState);
            ThemeManager.getInstance().animateToggle(tempDarkModeState);
        });
    }

    // ==========================================
    // ОНБОРДИНГ
    // ==========================================

    @FXML
    private void handleOnboardingStart() {
        String name = onboardingNameField.getText().trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isEmpty() || name.equalsIgnoreCase("Guest")) {
            onboardingNameField.getStyleClass().add("input-error");
            return;
        }

        File newProfile = new File(ROOT_MUSIC_DIR, name);
        if (!newProfile.exists()) newProfile.mkdirs();

        profileController.currentProfileName = name;
        profileController.profileFolder = newProfile;
        StatsManager.getInstance().setProfileFolder(newProfile);
        settingsController.saveAppConfig();

        mainPlayBtn.setDisable(false);
        profileNameLabel.setText(name);

        FadeTransition ft = new FadeTransition(Duration.millis(400), onboardingOverlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            onboardingOverlay.setVisible(false);
            modalManager.toggleGlassmorphism(false);
            loadingOverlay.setVisible(true);
            continueNormalStartup();
        });
        ft.play();
    }

    // ==========================================
    // СТАТИСТИКА
    // ==========================================

    public void updateListeningUI() {
        long total = StatsManager.getInstance().getTotalListeningSeconds();
        int totalHours = (int) (total / 3600);
        int totalMins = (int) ((total % 3600) / 60);
        homeTotalListened.setText(totalHours > 0
                ? String.format("%dh %02dm", totalHours, totalMins)
                : String.format("%dm", totalMins));

        long weekly = StatsManager.getInstance().getWeeklySeconds();
        int weekHours = (int) (weekly / 3600);
        int weekMins = (int) ((weekly % 3600) / 60);
        homeWeeklyListened.setText(weekHours > 0
                ? String.format("%dh %02dm", weekHours, weekMins)
                : String.format("%dm", weekMins));
    }

    // ==========================================
    // ОЧЕРЕДЬ
    // ==========================================

    public void addToQueue(Track track) {
        playQueue.add(track);
        updateQueueIndicator();
        modalManager.showToast("Added to queue: " + track.getTitle());
    }

    private void clearQueue() {
        playQueue.clear();
        isQueueMode = false;
    }

    public void updateQueueIndicator() {
        if (queueIcon == null) return;
        if (playQueue.isEmpty()) {
            queueIcon.getStyleClass().removeAll("icon-accent");
            if (!queueIcon.getStyleClass().contains("icon-gray"))
                queueIcon.getStyleClass().add("icon-gray");
            queueIcon.setEffect(null);
        } else {
            queueIcon.getStyleClass().removeAll("icon-gray");
            if (!queueIcon.getStyleClass().contains("icon-accent"))
                queueIcon.getStyleClass().add("icon-accent");
            queueIcon.setEffect(new javafx.scene.effect.DropShadow(
                    10, Color.web("#B388FF88")));
        }
    }

    @FXML private void onQueueIconClick() {
        if (playQueue.isEmpty()) { modalManager.showToast("Queue is empty"); return; }
        showQueuePopup();
    }

    @FXML private void onQueueIndicatorClick() { showQueuePopup(); }

    private void showQueuePopup() {
        if (playQueue.isEmpty() || queueIcon == null) return;

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);

        boolean isDark = ThemeManager.getInstance().isDarkMode();
        String base = ThemeManager.getInstance().getCurrentBaseTheme();

        String bg, border, hoverBg;
        String textMain = isDark ? "white" : "#121212";
        String textSub  = isDark ? "#ADA9BA" : "#706F8E";
        String errorColor = "#FF5252";

        switch (base) {
            case "Red"    -> { bg = isDark ? "#2C1C1C" : "#FFF0F0"; border = isDark ? "#4A2C2C" : "#FFD6D6"; hoverBg = isDark ? "#3D2626" : "#FFE6E6"; }
            case "Green"  -> { bg = isDark ? "#1C2C1F" : "#F0FFF4"; border = isDark ? "#2C4A32" : "#D6FFDE"; hoverBg = isDark ? "#263D2A" : "#E6FFE6"; }
            case "Cream"  -> { bg = isDark ? "#2C271C" : "#FFFCF0"; border = isDark ? "#4A402C" : "#FFE8B3"; hoverBg = isDark ? "#3D3526" : "#FFF4D6"; }
            case "Pink"   -> { bg = isDark ? "#2C1C23" : "#FFF0F5"; border = isDark ? "#4A2C39" : "#FFD6E8"; hoverBg = isDark ? "#3D2630" : "#FFE6F0"; }
            case "Nordic" -> { bg = isDark ? "#2E3440" : "#ECEFF4"; border = isDark ? "#434C5E" : "#D8DEE9"; hoverBg = isDark ? "#3B4252" : "#E5E9F0"; }
            case "Penguin"-> { bg = isDark ? "#121212" : "#FFFFFF"; border = isDark ? "#333333" : "#E0E0E0"; hoverBg = isDark ? "#1E1E1E" : "#F5F5F5"; }
            default       -> { bg = isDark ? "#1E1E2E" : "#FFFFFF"; border = isDark ? "#393A5A" : "#D1D1D6"; hoverBg = isDark ? "#2A2B40" : "#E9E9E9"; }
        }

        VBox container = new VBox(0);
        container.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 16;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 16;" +
                        "-fx-border-width: 1; -fx-padding: 0;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 8);");
        container.setPrefWidth(280);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 16 20 12 20;");

        FontIcon listIcon = new FontIcon("mdi-format-list-bulleted");
        listIcon.setIconSize(18);
        listIcon.setIconColor(Color.web(textMain));

        Label headerLabel = new Label("Play Queue");
        headerLabel.setStyle("-fx-text-fill: " + textMain + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label countLabel = new Label(playQueue.size() + " tracks");
        countLabel.setStyle("-fx-text-fill: " + textSub + "; -fx-font-size: 12px;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label clearBtn = new Label("Clear all");
        clearBtn.setStyle("-fx-text-fill: " + errorColor + "; -fx-font-size: 12px; -fx-cursor: hand;");
        clearBtn.setOnMouseClicked(e -> { clearQueue(); updateQueueIndicator(); popup.hide(); });

        header.getChildren().addAll(listIcon, headerLabel, countLabel, headerSpacer, clearBtn);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + border + ";");

        container.getChildren().addAll(header, divider);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(300);

        VBox trackList = new VBox(0);
        trackList.setStyle("-fx-background-color: transparent; -fx-padding: 8 0 8 0;");

        int limit = Math.min(playQueue.size(), 15);
        for (int i = 0; i < limit; i++) {
            Track t = playQueue.get(i);
            final int idx = i;

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 8 20 8 20; -fx-cursor: hand;");

            Label numLabel = new Label(String.valueOf(i + 1));
            numLabel.setStyle("-fx-text-fill: " + textSub + "; -fx-font-size: 12px; -fx-min-width: 20;");

            ImageView coverView = new ImageView();
            coverView.setFitWidth(36); coverView.setFitHeight(36); coverView.setSmooth(true);
            Rectangle coverClip = new Rectangle(36, 36);
            coverClip.setArcWidth(8); coverClip.setArcHeight(8);
            coverView.setClip(coverClip);
            setupObjectFitCover(coverView);
            if (t.getCover() != null) coverView.setImage(t.getCover());
            else {
                var res = getClass().getResource("default_cover.png");
                if (res != null) coverView.setImage(new Image(res.toExternalForm(), 36, 36, true, true));
            }

            VBox textBox = new VBox(2);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            Label titleLbl = new Label(t.getTitle());
            titleLbl.setStyle("-fx-text-fill: " + textMain + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            titleLbl.setMaxWidth(140);
            titleLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
            Label artistLbl = new Label(t.getArtist());
            artistLbl.setStyle("-fx-text-fill: " + textSub + "; -fx-font-size: 11px;");
            textBox.getChildren().addAll(titleLbl, artistLbl);

            FontIcon removeIcon = new FontIcon("mdi-close");
            removeIcon.setIconSize(14);
            removeIcon.getStyleClass().add("icon-gray");
            removeIcon.setOpacity(0);

            row.getChildren().addAll(numLabel, coverView, textBox, removeIcon);

            final String hoverBgFinal = hoverBg;
            row.setOnMouseEntered(e -> {
                row.setStyle("-fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-background-color: " + hoverBgFinal + ";");
                removeIcon.setOpacity(1);
            });
            row.setOnMouseExited(e -> {
                row.setStyle("-fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-background-color: transparent;");
                removeIcon.setOpacity(0);
            });
            row.setOnMouseClicked(e -> {
                if (e.getTarget() == removeIcon || e.getTarget() == removeIcon.getParent()) return;
                playQueue.subList(0, idx).clear();
                Track toPlay = playQueue.remove(0);
                updateQueueIndicator();
                popup.hide();
                playerController.startPlayback(toPlay);
            });
            removeIcon.setOnMouseClicked(e -> {
                e.consume();
                playQueue.remove(idx);
                updateQueueIndicator();
                popup.hide();
                if (!playQueue.isEmpty()) Platform.runLater(this::showQueuePopup);
            });

            trackList.getChildren().add(row);
            if (i < limit - 1) {
                Region rowDivider = new Region();
                rowDivider.setPrefHeight(1);
                rowDivider.setStyle("-fx-background-color: " + border + "; -fx-opacity: 0.4;");
                trackList.getChildren().add(rowDivider);
            }
        }

        if (playQueue.size() > 15) {
            Label moreLabel = new Label("+" + (playQueue.size() - 15) + " more tracks");
            moreLabel.setStyle("-fx-text-fill: " + textSub + "; -fx-font-size: 12px; -fx-padding: 8 20 8 20;");
            trackList.getChildren().add(moreLabel);
        }

        scroll.setContent(trackList);
        container.getChildren().add(scroll);
        popup.getContent().add(container);

        javafx.geometry.Bounds bounds = queueIcon.localToScreen(queueIcon.getBoundsInLocal());
        popup.show(queueIcon.getScene().getWindow(),
                bounds.getMinX() - 220, bounds.getMinY() - container.getPrefHeight() - 10);

        container.setOpacity(0);
        container.setTranslateY(10);
        new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(container.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(container.translateYProperty(), 0, Interpolator.EASE_OUT)
        )).play();
    }

    // ==========================================
    // КОНТЕКСТНОЕ МЕНЮ ТАБЛИЦЫ
    // ==========================================

    private void setupTableContextMenu() {
        trackTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        trackTable.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>() {
                @Override
                protected void updateItem(Track item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        getStyleClass().remove("playing-row");
                    } else {
                        if (playerController.mediaPlayer != null
                                && playerController.mediaPlayer.getMedia() != null) {
                            String playingURI = playerController.mediaPlayer.getMedia().getSource();
                            if (item.getFile().toURI().toString().equals(playingURI)) {
                                if (!getStyleClass().contains("playing-row"))
                                    getStyleClass().add("playing-row");
                            } else {
                                getStyleClass().remove("playing-row");
                            }
                        } else {
                            getStyleClass().remove("playing-row");
                        }
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getStyleClass().add("context-menu");

            MenuItem deleteSongItem = new MenuItem("Delete Song");
            deleteSongItem.getStyleClass().add("delete-menu-item");
            FontIcon trashIcon = new FontIcon("mdi-delete");
            trashIcon.setIconColor(Color.web("#FF5252"));
            deleteSongItem.setGraphic(trashIcon);
            deleteSongItem.setOnAction(event -> {
                if (!row.isEmpty()) libraryController.deleteTrack(row.getItem());
            });

            Menu addToPlaylistMenu = new Menu("Add to Playlist...");
            FontIcon addIcon = new FontIcon("mdi-playlist-plus");
            addIcon.getStyleClass().add("icon-gray");
            addToPlaylistMenu.setGraphic(addIcon);

            MenuItem removeFromPlaylist = new MenuItem("Remove from Playlist");
            FontIcon removeIcon = new FontIcon("mdi-playlist-remove");
            removeIcon.setIconColor(Color.web("#FFB74D"));
            removeFromPlaylist.setGraphic(removeIcon);
            removeFromPlaylist.setOnAction(e -> {
                if (!row.isEmpty() && activePlaylist != null) {
                    Track track = row.getItem();
                    PlaylistManager.removeTrackFromPlaylist(
                            getCurrentMusicFolder(), activePlaylist, track.getFile().getName());
                    trackListData.remove(track);
                    Platform.runLater(sidebarController::loadPlaylistsUI);
                    libraryController.updateLibraryStats();
                    trackTable.refresh();
                }
            });

            MenuItem editTrackItem = new MenuItem("Edit Song Info");
            FontIcon editIcon = new FontIcon("mdi-pencil");
            editIcon.getStyleClass().add("icon-gray");
            editTrackItem.setGraphic(editIcon);
            editTrackItem.setOnAction(event -> {
                if (!row.isEmpty()) modalManager.openEditTrackModal(row.getItem());
            });

            MenuItem addToQueueItem = new MenuItem("Add to Queue");
            FontIcon queueIconMenu = new FontIcon("mdi-playlist-play");
            queueIconMenu.getStyleClass().add("icon-gray");
            addToQueueItem.setGraphic(queueIconMenu);
            addToQueueItem.setOnAction(event -> {
                if (!row.isEmpty()) addToQueue(row.getItem());
            });

            row.setOnContextMenuRequested(event -> {
                if (row.isEmpty()) { contextMenu.hide(); return; }

                trackTable.getSelectionModel().select(row.getItem());
                contextMenu.getItems().clear();
                addToPlaylistMenu.getItems().clear();

                Track selectedTrack = row.getItem();
                String trackFileName = selectedTrack.getFile().getName();
                List<String> playlists = PlaylistManager.getAllPlaylists(getCurrentMusicFolder());

                if (playlists.isEmpty()) {
                    MenuItem emptyItem = new MenuItem("No playlists yet");
                    emptyItem.setDisable(true);
                    addToPlaylistMenu.getItems().add(emptyItem);
                } else {
                    for (String pName : playlists) {
                        MenuItem pItem = new MenuItem(pName);
                        List<String> existing = PlaylistManager.getTracksFromPlaylist(
                                getCurrentMusicFolder(), pName);
                        boolean alreadyAdded = existing.stream()
                                .anyMatch(t -> t.trim().equals(trackFileName.trim()));
                        if (alreadyAdded) {
                            pItem.setText(pName + "  (Added)");
                            pItem.setDisable(true);
                        } else {
                            pItem.setOnAction(ev -> {
                                PlaylistManager.addTrackToPlaylist(
                                        getCurrentMusicFolder(), pName, trackFileName);
                                Platform.runLater(sidebarController::loadPlaylistsUI);
                            });
                        }
                        addToPlaylistMenu.getItems().add(pItem);
                    }
                }

                if (activePlaylist != null) {
                    contextMenu.getItems().addAll(addToQueueItem, editTrackItem,
                            addToPlaylistMenu, removeFromPlaylist, new SeparatorMenuItem(), deleteSongItem);
                } else {
                    contextMenu.getItems().addAll(addToQueueItem, editTrackItem,
                            addToPlaylistMenu, new SeparatorMenuItem(), deleteSongItem);
                }
            });

            row.setContextMenu(contextMenu);
            return row;
        });
    }

    // ==========================================
    // НАВИГАЦИЯ (FXML делегаты)
    // ==========================================

    public void navigateTo(String viewName, boolean saveToHistory) {
        navigationController.navigateTo(viewName, saveToHistory);
    }
    @FXML private void onBackClick()    { navigationController.onBackClick(); }
    @FXML private void onForwardClick() { navigationController.onForwardClick(); }
    @FXML private void onHomeClick()    { navigationController.onHomeClick(); }
    @FXML private void onLibraryClick() { navigationController.onLibraryClick(); }

    // ==========================================
    // ПЛЕЕР (FXML делегаты)
    // ==========================================

    @FXML
    public void onPlayClick()     { playerController.onPlayClick(); }
    @FXML
    public void onNextClick()     { playerController.onNextClick(); }
    @FXML
    public void onPreviousClick() { playerController.onPreviousClick(); }
    @FXML void onShuffleClick()  { playerController.onShuffleClick(); }
    @FXML void onRepeatClick()   { playerController.onRepeatClick(); }
    @FXML void onVolumeClick()   { playerController.onVolumeClick(); }
    @FXML void onSliderPressed() { playerController.onSliderPressed(); }
    @FXML void onSliderReleased(){ playerController.onSliderReleased(); }
    @FXML void onSliderClicked(javafx.scene.input.MouseEvent e) { playerController.onSliderClicked(e); }

    // Публичные прокси для других классов
    public void prepareTrack(java.io.File file) { playerController.prepareTrack(file); }
    public void startPlayback(Track track)       { playerController.startPlayback(track); }
    public void updateUI(Track track)            { playerController.updateUI(track); }
    public void updateSliderGradient()           { playerController.updateSliderGradient(); }
    public void updateVolumeGradient()           { playerController.updateVolumeGradient(); }
    public void updateMarquee()                  { playerController.updateMarquee(); }
    public String formatTime(Duration d)         { return playerController.formatTime(d); }

    // ==========================================
    // МОДАЛКИ (FXML делегаты)
    // ==========================================

    @FXML public void openAddTrackModal()       { modalManager.openAddTrackModal(); }
    @FXML private void closeAddTrackModal()     { modalManager.closeAddTrackModal(); }
    @FXML private void onChooseCoverClick()     { modalManager.onChooseCoverClick(); }
    @FXML private void onChooseAudioClick()     { modalManager.onChooseAudioClick(); }
    @FXML private void onSaveNewTrackClick()    { modalManager.onSaveNewTrackClick(); }
    @FXML private void switchToAddManual()      { modalManager.switchToAddManual(); }
    @FXML private void switchToAddLink()        { modalManager.switchToAddLink(); }
    @FXML private void onDownloadFromLinkClick(){ modalManager.onDownloadFromLinkClick(); }
    @FXML private void closeEditTrackModal()    { modalManager.closeEditTrackModal(); }
    @FXML private void onSaveEditedTrackClick() { modalManager.onSaveEditedTrackClick(); }
    @FXML private void onChooseEditCoverClick() { modalManager.onChooseEditCoverClick(); }
    @FXML public void onAddPlaylistClick()      { modalManager.onAddPlaylistClick(); }
    @FXML private void closePlaylistModal()     { modalManager.closePlaylistModal(); }
    @FXML private void onChoosePlaylistCoverClick() { modalManager.onChoosePlaylistCoverClick(); }
    @FXML private void saveNewPlaylist()        { modalManager.saveNewPlaylist(); }
    @FXML private void switchToPlaylistManual() { modalManager.switchToPlaylistManual(); }
    @FXML private void switchToPlaylistLink()   { modalManager.switchToPlaylistLink(); }
    @FXML private void onImportPlaylistClick()  { modalManager.onImportPlaylistClick(); }

    // ==========================================
    // НАСТРОЙКИ (FXML делегаты)
    // ==========================================

    @FXML private void openSettingsModal()  { settingsController.openSettingsModal(); }
    @FXML private void saveSettings()       { settingsController.saveSettings(); }
    @FXML private void closeSettingsModal() { settingsController.closeSettingsModal(); }
    public void loadAppConfig()             { settingsController.loadAppConfig(); }
    public void saveAppConfig()             { settingsController.saveAppConfig(); }

    // ==========================================
    // ПРОФИЛЬ (FXML делегаты)
    // ==========================================

    @FXML private void onProfileBtnClick(javafx.scene.input.MouseEvent e) {
        profileController.onProfileBtnClick(e); }
    @FXML private void closeProfileModal()      { profileController.closeProfileModal(); }
    @FXML private void saveNewProfile()         { profileController.saveNewProfile(); }
    @FXML private void closeDeleteConfirmModal(){ profileController.closeDeleteConfirmModal(); }
    @FXML private void confirmDeleteProfile()   { profileController.confirmDeleteProfile(); }

    // ==========================================
    // БИБЛИОТЕКА (публичные прокси)
    // ==========================================

    public void loadMusicLibrary()              { libraryController.loadMusicLibrary(); }
    public void calculateDurations()            { libraryController.calculateDurations(); }
    public void updateLibraryStats()            { libraryController.updateLibraryStats(); }
    public void deleteTrack(Track track)        { libraryController.deleteTrack(track); }
    public void convertWebpThumbnailsToJpg()    { libraryController.convertWebpThumbnailsToJpg(); }
    public Image loadSmallCover(java.io.File f, String name) {
        return libraryController.loadSmallCover(f, name); }

    // ==========================================
    // UI ХЕЛПЕРЫ
    // ==========================================

    public void setupObjectFitCover(ImageView imageView) {
        imageView.imageProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                Runnable applyCrop = () -> {
                    double w = newImg.getWidth();
                    double h = newImg.getHeight();
                    if (w > 0 && h > 0) {
                        double size = Math.min(w, h);
                        double x = (w - size) / 2;
                        double y = (h - size) / 2;
                        imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, size, size));
                    }
                };
                if (newImg.getProgress() == 1.0) applyCrop.run();
                else newImg.progressProperty().addListener((o, oldP, newP) -> {
                    if (newP.doubleValue() == 1.0) applyCrop.run();
                });
            } else {
                imageView.setViewport(null);
            }
        });
    }

    public void setupSmoothCardHover(Node card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02);
            st.setInterpolator(Interpolator.EASE_OUT); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT); st.play();
        });
    }

    private void setupHeroCardAnimation(Node card) {
        String normalShadow = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 8);";
        String glowShadow   = "-fx-effect: dropshadow(three-pass-box, -accent-purple, 35, 0.3, 0, 0);";
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02); st.play();
            card.setStyle(glowShadow);
            card.setViewOrder(-1.0);
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
            card.setStyle(normalShadow);
            card.setViewOrder(0.0);
        });
    }

    private void setupWindowBtn(ImageView btn) {
        if (btn.getEffect() == null) btn.setEffect(new javafx.scene.effect.ColorAdjust());
        btn.setOpacity(0.6);
        btn.setOnMouseEntered(e -> {
            btn.setOpacity(1.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.2); st.setToY(1.2); st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setOpacity(0.6);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void setupModalCloseBtn(ImageView btn) {
        if (btn == null) return;
        if (btn.getEffect() == null) btn.setEffect(new javafx.scene.effect.ColorAdjust());
        btn.setOpacity(0.6);
        btn.setOnMouseEntered(e -> {
            btn.setOpacity(1.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.2); st.setToY(1.2); st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setOpacity(0.6);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void setupWindowBtnProfile(StackPane btn) {
        if (btn == null) return;
        btn.setCache(true);
        btn.setCacheHint(CacheHint.SCALE);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setOpacity(0.8);
        btn.setOnMouseEntered(e -> {
            btn.setOpacity(1.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(250), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(1.15); st.setToY(1.15); st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setOpacity(0.8);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setInterpolator(Interpolator.EASE_IN);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        btn.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(0.9); st.setToY(0.9); st.play();
        });
        btn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(1.15); st.setToY(1.15); st.play();
        });
    }

    private void setupVectorIconEffects(Region icon) {
        if (icon == null) return;
        icon.setOnMouseEntered(e -> {
            if (!icon.isDisable()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), icon);
                st.setToX(1.2); st.setToY(1.2); st.play();
            }
        });
        icon.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), icon);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        icon.setOnMousePressed(e -> {
            if (!icon.isDisable()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(80), icon);
                st.setToX(0.9); st.setToY(0.9); st.play();
            }
        });
        icon.setOnMouseReleased(e -> {
            if (!icon.isDisable()) {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), icon);
                st.setToX(1.2); st.setToY(1.2); st.play();
            }
        });
    }

    private void preventDoubleClickMaximize(Node node) {
        if (node != null) {
            node.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() == 2) event.consume();
            });
        }
    }

    // Публичный прокси для DashboardBuilder
    public HBox createAnimatedEqualizer() {
        return dashboardBuilder.createAnimatedEqualizer();
    }

    public static String getAppDir() {
        String path = System.getProperty("app.dir");
        if (path != null) return path;
        return System.getProperty("user.dir");
    }
}