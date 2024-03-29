package resources;

import helperClasses.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mainController implements Initializable, Serializable {
    //---------------------------------- Helper classes ---------------------------------
    private AlertGenerator _alertGenerator = new AlertGenerator();
    private Alert _alert;
    private ExecutorService _flickrService;

    //---------------------------------- Query fields ----------------------------------------------
    private String _path, _searchTerm, _searchText, _audioFileName;
    private TextInputDialog audioFileNamePrompt;

    //---------------------------------- General fields --------------------------------------------
    private boolean _selectedPressed;
    private WikiSearch _searchWikiTask;
    private FlickrClass _flickrTask;


    /**************************** START OF CREATION TAB COMPONENTS **********************************/

    /* ========================== START OF SEARCH PANE COMPONENTS ================================= */

    @FXML private AnchorPane searchTextPane;
    @FXML private TextField searchField;
    @FXML private Label searchingLabel;
    @FXML private Button searchBtn, searchCancelBtn, addTextBtn;
    @FXML private TextArea searchTextArea, selectedTextArea;
    @FXML private ProgressBar searchProgress;

    /* ========================== END OF SEARCH PANE COMPONENTS ================================= */

    /* ========================== START OF AUDIO PANE COMPONENTS ================================= */

    @FXML private AnchorPane createAudioPane;
    @FXML private TextArea audioDisplayText;
    @FXML private ChoiceBox<String> audioChoiceBox, backgroundChoiceBox;
    @FXML private ListView<String> audioCreationsList;
    @FXML public static MediaPlayer previewPlayer;
    @FXML private Label selectedAudioLabel;

    /* ========================== END OF AUDIO PANE COMPONENTS ================================= */

    /* ========================== START OF FLICKR PANE COMPONENTS ============================== */

    @FXML private AnchorPane flickrPane;
    @FXML private Label flickrSceneLabel;
    @FXML private ProgressBar flickrProgress;
    @FXML private ToggleButton imageBtn1, imageBtn2, imageBtn3, imageBtn4, imageBtn5, imageBtn6, imageBtn7, imageBtn8,
    imageBtn9, imageBtn10;
    @FXML private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9, image10;
    @FXML private TextField creationNameField;
    @FXML private Button finishFlickrBtn;

    private CreateVideoTask _videoTask;
    private ImageView[] images;
    private ToggleButton[] imageButtons;

    /* ========================== END OF FLICKR PANE COMPONENTS ============================== */


    /**************************** END OF CREATION TAB COMPONENTS **********************************/


    /**************************** START OF REVIEW TAB COMPONENTS **********************************/

    @FXML private TableView<Creation> tableViewForReview;
    @FXML private TableColumn<Creation, String> _tableFileName; // column 1
    @FXML private TableColumn<Creation, String> _tableKeyword; // column 2
    @FXML private TableColumn<Creation, String> _confidenceLevel; // column 3
    @FXML private TableColumn<Creation, String> _numberOfPlays; // column 4
    private List<Creation> _existingCreations = new ArrayList<Creation>();;

    @FXML private MediaView reviewMediaView;
    private MediaPlayer reviewMediaPlayer;
    private Creation _currentlyPlaying;


    /**************************** END OF REVIEW TAB COMPONENTS **********************************/

    /**
     * Initialises all the functionality and logic for the GUI components
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //------------------------- Create the string directory to the working folder -----------
        String myDirectory = "206project_team17"; // user Folder Name
        String users_home = System.getProperty("user.home");
        _path = users_home.replace("\\", "/") + File.separator + myDirectory;
        new File(_path).mkdir();

        //Create the creations.txt file which stores the object info if it doesn't exist
        try {
            genericProcess("touch " + _path + "/creations.txt");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        //Update the tableview with existing creations
        try {
            updateCreations();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //-------------------------- SET UP SEARCH PANE COMPONENTS -----------------------------------

        searchTextPane.toFront();
        _searchTerm = "";
        searchCancelBtn.setVisible(false);
        searchingLabel.setVisible(false);
        searchProgress.setVisible(false);
        searchTextArea.setEditable(false);

        BooleanBinding isAddTextAreaEmpty = Bindings.isEmpty(searchTextArea.textProperty());
        addTextBtn.disableProperty().bind(isAddTextAreaEmpty);

        //-------------------------- SET UP AUDIO PANE COMPONENTS ----------------------------

        try {
            generateScm();
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioChoiceBox.getItems().add("Default Voice");
        audioChoiceBox.getItems().add("AKL Accent");
        audioChoiceBox.setValue("Default Voice");

        backgroundChoiceBox.getItems().add("forgottenland");
        backgroundChoiceBox.getItems().add("magicyworld");
        backgroundChoiceBox.getItems().add("newyorkcity");
        backgroundChoiceBox.getItems().add("no background music");
        backgroundChoiceBox.setValue("forgottenland");

        audioFileNamePrompt = new TextInputDialog();
        audioFileNamePrompt.setHeaderText("Enter a name for audio file");
        audioFileNamePrompt.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        audioCreationsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshAudioList();

        //-------------------------- SET UP FLICKR PANE COMPONENTS ------------------------

        images = new ImageView[10];
        images[0] = image1;
        images[1] = image2;
        images[2] = image3;
        images[3] = image4;
        images[4] = image5;
        images[5] = image6;
        images[6] = image7;
        images[7] = image8;
        images[8] = image9;
        images[9] = image10;

        imageButtons = new ToggleButton[10];
        imageButtons[0] = imageBtn1;
        imageButtons[1] = imageBtn2;
        imageButtons[2] = imageBtn3;
        imageButtons[3] = imageBtn4;
        imageButtons[4] = imageBtn5;
        imageButtons[5] = imageBtn6;
        imageButtons[6] = imageBtn7;
        imageButtons[7] = imageBtn8;
        imageButtons[8] = imageBtn9;
        imageButtons[9] = imageBtn10;


        //-------------------------- SET UP REVIEW TAB COMPONENTS -------------------------

        // set up table view
        tableViewForReview.getItems().setAll(this._existingCreations);
        _tableFileName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        _tableKeyword.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKeyword()));
        _numberOfPlays
                .setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getNumberOfPlays())));
        _confidenceLevel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConfidenceLevel()));
        editableConfidence();
    }

    /*************************** START OF CREATION TAB LOGIC ***********************************/

    /*===========================START OF SEARCH PANE LOGIC  ==================================*/

    /**
     * On button click, the _searchTerm is checked then a task is created for the wikit bash commmand and submitted to the executor service
     * @param event
     * @throws Throwable
     */
    @FXML void searchBtnPressed(ActionEvent event) throws Throwable {
        //When the button is clicked, Get the search term
        _searchTerm = searchField.getText();
        //Check for valid input
        if (_searchTerm.isEmpty() || _searchTerm.trim().isEmpty() || !isAlphanumeric(_searchTerm) || !isAlphanumeric2(_searchTerm)) {

            // Show alert if search term is empty
            _alert = _alertGenerator.newAlert("Invalid Input", "Invalid search term", "Please enter a valid term to search", "error");
            _alert.showAndWait();

        } else {
            _searchWikiTask = new WikiSearch(_searchTerm);

            _searchWikiTask.setOnRunning((succeesesEvent) -> {
                setUpSearchWikiOnRunning();
            });

            _searchWikiTask.setOnCancelled((taskCancelled) ->{
                setUpSearchWikiOnCancelled();
            });

            searchProgress.progressProperty().bind(_searchWikiTask.progressProperty());

            // When the thread finished its task
            _searchWikiTask.setOnSucceeded((succeededEvent) -> {
                try {
                    setUpSearchWikiOnSuccess();
                    String searchWikiLine = getSearchResultProcess();
                    String invalidResult = _searchTerm + " not found :^(";

                    if (searchWikiLine.equals(invalidResult)) {
                        // Invalid input, display an alert
                        _alert = _alertGenerator.newAlert("Invalid Input", "Invalid Term", "No results available, please enter a valid input", "error");
                        _alert.showAndWait();

                    } else {
                        setUpSearchResult();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            });

            //Submit to executor service
            ExecutorService searchService = Executors.newFixedThreadPool(1);
            searchService.execute(_searchWikiTask);
            searchService.shutdown();
        }
    }

    /**
     * Cancels the wikit task
     * @param event
     */
    @FXML void searchCancelBtnPressed(ActionEvent event) {
        _searchWikiTask.cancel();
    }


    /**
     * Copy the highlighted text to the selectedTextArea
     * @param event
     */
    @FXML void addBtnPressed(ActionEvent event) {
        String selectedText = searchTextArea.getSelectedText();
        selectedTextArea.setText(selectedText);
    }

    /**
     * Checks whether the selected text has a valid word count then previews a festival TTS to the user
     * @param event
     * @throws Throwable
     */
    @FXML void previewSelectedBtnPressed(ActionEvent event) throws Throwable {
        // Play the selected text
        String str = selectedTextArea.getText();
        int words = getWordCounts(str);

        if (selectedTextArea.getText().isEmpty() || selectedTextArea.getText().trim().isEmpty()) {
            //do nothing
        } else if (words < 10 || words > 40) {
            //Selected text has too little/many words
            _alert = _alertGenerator.newAlert("Invalid Input", "Invalid number of words", "Please select 10-40 words only", "error");
            _alert.showAndWait();

        } else {
            String content = selectedTextArea.getText().replaceAll("[\\[\\](){}']", "");
            try {
                //TTS the selected audio
                String previewCmd = "echo " + content + " | festival --tts";
                ProcessBuilder previewPb = new ProcessBuilder("bash", "-c", previewCmd);
                Process previewProcess = previewPb.start();

            } catch (IOException previewException) {
                previewException.printStackTrace();
            }
        }
    }

    /**
     * Clears the selectedTextArea
     * @param event
     */
    @FXML void clearSelectionBtnPressed(ActionEvent event) {
        selectedTextArea.clear();
    }

    /**
     * Gets the selected text and updates to the next scene then directs the user to the next scene
     * @param event
     * @throws Throwable
     */
    @FXML void searchNextBtnPressed(ActionEvent event) throws Throwable {
        audioDisplayText.setText(selectedTextArea.getText());
        //Checks whether the selected text has a valid word count
        if(getWordCounts(selectedTextArea.getText()) < 41 && getWordCounts(selectedTextArea.getText()) > 9) {
            createAudioPane.toFront();
        }
        else{
            //Otherwise alert the user
            _alert = _alertGenerator.newAlert("Invalid Input", "Invalid word count", "Please have 10 - 40 words only in your selected text area", "error");
            _alert.showAndWait();
        }
    }

    /**
     * Shows a pop up to the user showing the instructions for searching a term in this step
     * @param event
     * @throws Throwable
     */
    @FXML void searchHelpBtnPressed(ActionEvent event) throws Throwable {
        _alert = _alertGenerator.newAlert("", "", "" +
                "1. Enter a search term in the search box then press 'Enter' or click on the 'Magnifying Glass' button to search\n\n" +
                "2. If your search is taking too long because no results are appearing, press the 'Cance;' button to try again\n\n" +
                "2. Highlight the text from the left text box that you want in your creation by dragging your mouse over the text you want\n\n" +
                "3. Click the 'Add' button to add your text to the right textbox\n\n" +
                "4. Press the 'Preview' or 'Clear' button to either: Preview the audio or Clear your selection\n\n" +
                "5. Press the 'Next' button to move onto the audio section when you are happy with your text", "information");
        _alert.show();
    }


    /*=========================== END OF SEARCH PANE LOGIC ==================================*/


    /*=========================== START OF AUDIO PANE LOGIC ===============================*/

    /**
     * Checks whether the audio selected is valid then looks at all the audio options and generates an audio file
     * accordingly then notifies the user when the task is finished. Generated audio files shown in the list on the right.
     * @param event
     * @throws Throwable
     */
    @FXML void createAudioBtnPressed(ActionEvent event) throws Throwable {
        String synthesizer = audioChoiceBox.getValue();

        // Prompt the user for an audio name
        audioFileNamePrompt.showAndWait();
        String audioName = audioFileNamePrompt.getEditor().getText();

        // Determine whether it is a valid name
        if (audioName.isEmpty() || audioName.trim().isEmpty() || !isAlphanumeric2(audioName) || !isAlphanumeric(audioName)) {
            _alert = _alertGenerator.newAlert("Invalid name", "Empty input", "Please enter a valid name for this audio file", "error");
            _alert.showAndWait();

        } else {
            if (isValidName(audioName)) {
                try {
                    // Write the selected text to a .txt file
                    WriteToTxtFile.writeTxt(_path, "Selected.txt", selectedTextArea.getText());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Create the audio
                if (synthesizer == "AKL Accent") {
                    try {
                        AudioCreationTask audioTask = new AudioCreationTask(_path, backgroundChoiceBox.getValue(), audioName, synthesizer);

                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(audioTask);

                        if (isCreated() == false) {
                            // The audio file they created has some unpronounceable words for this synthesiser
                            _alert = _alertGenerator.newAlert("Error", "Unreadable words", "Text contains unreadable words, please change to the default voice or edit the text", "error");
                            _alert.showAndWait();
                            //Remove the audio file created
                            genericProcess("rm " + _path + "/" + audioName + ".wav");
                        } else {
                            // Show confirmation
                            audioTask.setOnSucceeded((onCompletion) ->{
                                executorService.shutdown();
                                // Alert the user that the audio file has been successfully created
                                try {
                                    _alert = _alertGenerator.newAlert("Audio completed", "Audio file generated", "The audio file " + audioName + " has been made", "information");
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                                _alert.showAndWait();
                                refreshAudioList();
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    refreshAudioList();

                } else {
                    // Use the default synthesizer
                    AudioCreationTask audioTask = new AudioCreationTask(_path, backgroundChoiceBox.getValue(), audioName, synthesizer);

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(audioTask);

                    audioTask.setOnSucceeded((onCompletion) ->{
                        executorService.shutdown();
                        // Alert the user that the audio file has been successfully created
                        try {
                            _alert = _alertGenerator.newAlert("Audio completed", "Audio file generated", "The audio file " + audioName + " has been made", "information");
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        _alert.showAndWait();
                        refreshAudioList();
                    });

                }

            } else {
                //Audio file already exists so alert the user
                _alert = _alertGenerator.newAlert("Invalid input", "Duplicate file", "File name already exists, enter another name", "error");
                _alert.showAndWait();

            }
        }
    }

    /**
     * Switches panes by going to the previous screen(search step)
     * @param event
     */
    @FXML void audioBackBtnPressed(ActionEvent event) {
        searchTextPane.toFront();
    }

    /**
     * Shows a pop up to the user showing instructions of how to create an audio file in this step
     * @param event
     * @throws Throwable
     */
    @FXML void audioHelpBtnPressed(ActionEvent event) throws Throwable {
        _alert = _alertGenerator.newAlert("", "", "" +
                "1. Your selected will be displayed in the left textbox\n\n" +
                "2. Below the textbox are two dropdown boxes where you can select your speech voice and background music\n\n" +
                "3. Once you are happy with your options, click the 'Create' button and wait for your audio file to be created\n\n" +
                "4. Select your audio file by clicking on the list then press the 'Preview' or 'Delete' button below the audio list to either: Preview the audio or Delete your selection\n\n" +
                "5. Once you are happy with your audio file, select the audio file you want by clicking on the list then pressing the 'Select' button\n\n" +
                "6. You will be able to see your selected audio file above the 'Next' button, once you are happy press the 'Next' button to continue to select your images", "information");
        _alert.show();
    }

    /**
     * Gets the selected audio files and combines them through our bash command. Upon successful combining, the user
     * is notified and the list is updated.
     * @param event
     * @throws Throwable
     */
    @FXML void combineBtnPressed(ActionEvent event) throws Throwable {
        ObservableList<String> selectedItems = audioCreationsList.getSelectionModel().getSelectedItems();
        String audioFileNames = "";
        int count = 0;
        // Must be a multiple selection
        for (String s : selectedItems) {
            audioFileNames = audioFileNames + _path + "/" + s + ".wav ";
            count++;

        }
        if (count > 1) {

            audioFileNamePrompt.showAndWait();
            String audioName = audioFileNamePrompt.getEditor().getText();

            // Determine whether it is a valid name
            if (audioName.isEmpty() || audioName.trim().isEmpty()) {
                _alert = _alertGenerator.newAlert("Invalid input", "Invalid FIle Name", "Please enter a non empty file name", "error");
                _alert.showAndWait();

            } else {
                if (isValidName(audioName)) {

                    // Combine wav files
                    try {
                        genericProcess("sox " + audioFileNames + _path + "/" + audioName + ".wav");
                    } catch (IOException invalidSoxCmdException) {
                        invalidSoxCmdException.printStackTrace();
                    }
                    // Alert the user that the audio file has been successfully created
                    _alert = _alertGenerator.newAlert("Audio completed", "Audio file generated", "THe audio file" + audioName + " has been made", "information");
                    _alert.showAndWait();
                    refreshAudioList();

                } else {
                    //Audio file already exists so alert the user
                    _alert = _alertGenerator.newAlert("Invalid input", "Duplicate file", "File name already exists, enter another name", "error");
                    _alert.showAndWait();

                }

            }
        }
        else {
            //The user has tried to combine less than 2 audio files, alert the user
            _alert = _alertGenerator.newAlert("Invalid Selection", "Not enough files selected", "Please highlight at least 2 audio files to combine", "error");
            _alert.showAndWait();

        }
        refreshAudioList();
    }

    /**
     * Gets the selected audio file and creates a MediaPlayer which can be paused by the user to preview their audio file
     * @param event
     */
    @FXML void previewAudioBtnPressed(ActionEvent event) {
        String fileName = audioCreationsList.getSelectionModel().getSelectedItem();

        if(fileName == null){
            // Do nohthing
            return;
        }
        PreviewAudio.previewAudio(_path, fileName);
    }

    /**
     * Stops the previewing audio
     * @param event
     */
    @FXML void stopPreviewBtnPressed(ActionEvent event) {
        previewPlayer.stop();
    }

    /**
     * When the user clicks on an audio file and presses 'Select', we update path of the audio file for video creation.
     * The bottom label displaying the selected audio file is also updated. We ony allow one selected audio file.
     * @param event
     * @throws Throwable
     */
    @FXML void selectAudioBtnPressed(ActionEvent event) throws Throwable {
        //User can only select one audio file to move onto the next section
        if(getSelectedAudioCreationsCount() > 1){
            _alert = _alertGenerator.newAlert("Invalid Selection", "Too many files", "Please select one file only for final creation", "error");
            _alert.showAndWait();

        }
        else {
            //getSelectedAudioCreationsCount() method updates the _audioFileName field which is the location of the audioFile to be used
            String splitName[] = _audioFileName.split("/");
            String displayFileName = splitName[splitName.length - 1];
            selectedAudioLabel.setText(displayFileName);
            _selectedPressed = true;
        }
    }

    /**
     * Gets the selected audio file and deletes it
     * @param event
     */
    @FXML void deleteAudioBtnPressed(ActionEvent event) {
        ObservableList<String> selectedItems = audioCreationsList.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            //Do nothing
        }
        else {
            for (String s : selectedItems) {
                File file = new File(_path + File.separator + s + ".wav");
                file.delete();

            }
            refreshAudioList();

        }
    }


    /**
     * Checks whether there is a selected audio file then alerts the user if there isn't. If there is a selected audio file,
     * we run a flickr task to search flickr for images for their searched term and display a progress bar on the next scene
     * while the user waits for the searches to appear. The toggle buttons on the flickr scene is then updated on successful
     * search
     * @param event
     * @throws Throwable
     */
    @FXML void audioNextBtnPressed(ActionEvent event) throws Throwable {
        if(_selectedPressed) {
            flickrProgress.setVisible(true);
            finishFlickrBtn.setDisable(true);
            flickrPane.toFront();
            //When input is valid, make a new task and submit to ExecutorServices and wait
            _flickrTask = new FlickrClass(_searchTerm, _path);
            _flickrService = Executors.newSingleThreadExecutor();
            _flickrService.submit(_flickrTask);

            _flickrTask.setOnRunning((whileRunning) ->{
                setUpFlickrOnRunning();
            });

            //When the thread finished its task prompt the user for a name for the file
            _flickrTask.setOnSucceeded((succeededEvent) -> {
                setUpFlickrOnSuccess();
            });
        }
        else {
            _alert = _alertGenerator.newAlert("Nothing selected", "Please choose an audio file", "Select an audio file by highlighting a file on the list and pressing select", "error");
            _alert.showAndWait();
        }
    }

    /*=========================== END OF AUDIO PANE LOGIC ===============================*/

    /*=========================== START OF FLICKR PANE LOGIC ===============================*/

    /**
     * Checks whether the entered creation name already exists and notifies the user if it already is. Also checks whether
     * the name is alphanumeric/valid. A video creation task is then created and submitted to an executor service. The user is notified
     * when the video creation is complete and takes them back to the search screen.
     * @param event
     * @throws Throwable
     */
    @FXML void finishFlickrBtnPressed(ActionEvent event) throws Throwable {
        String cmd = "test -e " + _path + "/" + creationNameField.getText() +".mp4";
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
        Process process = builder.start();
        process.waitFor();
        //Gets the exit value to see if the creation anme already exists
        int exitVal = process.exitValue();
        process.destroy();

        // If statements to check validity of the creation name
        if(creationNameField.getText().isEmpty() || !isAlphanumeric(creationNameField.getText()) || !isAlphanumeric2(creationNameField.getText())){
            _alert = _alertGenerator.newAlert("Invalid Input", "Invalid characters", "Please enter a name for this creation that is alphanumeric", "error");
            _alert.showAndWait();
        }
        else if(exitVal == 0){
            _alert = _alertGenerator.newAlert("Invalid input", "Duplicate file", "The creation name you have entered already exists", "error");
            _alert.showAndWait();
        }
        else {
            // User must select at least one image to create a video
            if(getSelectedImages() == null) {
                _alert = _alertGenerator.newAlert("No images", "No images selected", "You have not selected any images, please select at least one image", "Error");
                _alert.showAndWait();
                return;
            }
            else {
                int num = getSelectedImages().size();
                removeUnselectedImages();
                _audioFileName = _audioFileName.substring(0, _audioFileName.length() - 1);
                _videoTask = new CreateVideoTask(creationNameField.getText(), _searchTerm, _audioFileName, _path, num);
                //Submit task to executor service
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(_videoTask);

                _videoTask.setOnRunning((whileRunning) -> {
                    setUpVideoCreationOnRunning();
                });

                _videoTask.setOnSucceeded((whenFinished) -> {
                    executorService.shutdown();
                    setUpVideoCreationOnSuccess();
                });
            }
        }

    }

    /**
     * Goes back to the audio creation scene then removes already searched images
     * @param event
     * @throws IOException
     * @throws InterruptedException
     */
    @FXML void flickrBackBtnPressed(ActionEvent event) throws IOException, InterruptedException {
        createAudioPane.toFront();
        genericProcess("rm -r " + _path + "/temp/");
    }

    /**
     * Shows the user a pop up listing all the steps to create a video in this step
     * @param event
     * @throws Throwable
     */
    @FXML void flickrHelpBtnPressed(ActionEvent event) throws Throwable {
        _alert = _alertGenerator.newAlert("", "", "" +
                "1. Click on the images that you want\n\n" +
                "2. Selected images will be highlighted/have a blue border around them\n\n" +
                "3. Please choose at least one image\n\n" +
                "4. Once you are happy with your images, enter a creation name in the textfield below\n\n" +
                "5. Click the 'Finish' button to create your video\n\n", "information");
        _alert.show();
    }

    /*=========================== END OF FLICKR PANE LOGIC ===============================*/


    /**************************** END OF CREATION TAB LOGIC ***********************************/


    /************************** START OF REVIEW TAB LOGIC *************************************/

    /**
     * Gets the selected creation and updates the MediaView with a media player then begins to
     * play the creaiton for the user
     * @param event
     */
    @FXML void reviewStartPlayBtnPressed(ActionEvent event) {
        Creation selection = tableViewForReview.getSelectionModel().getSelectedItem();
        if(selection == null){
            //do nothing
            return;
        }
        //If there already is something playing, stop it
        if(reviewMediaPlayer != null){
            reviewMediaPlayer.stop();
        }
        _currentlyPlaying = selection;
        String videoName = selection.getName();

        //Set up the media player
        String path = _path + "/" + videoName + ".mp4";
        Media media = new Media(new File(path).toURI().toString());
        reviewMediaPlayer = new MediaPlayer(media);
        reviewMediaView.setMediaPlayer(reviewMediaPlayer);
        reviewMediaPlayer.setAutoPlay(true);

        reviewMediaPlayer.setOnEndOfMedia(() -> {
            _currentlyPlaying.increaseNumberOfPlays();
            tableViewForReview.refresh();
            try {
                saveCreations(_existingCreations);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Plays/resumes the video
     * @param event
     */
    @FXML void reviewPlayBtnPressed(ActionEvent event) {
        reviewMediaPlayer.play();
    }

    /**
     * Pauses the video
     * @param event
     */
    @FXML void reviewPauseBtnPressed(ActionEvent event) {
        reviewMediaPlayer.pause();
    }

    /**
     * Stops the video
     * @param event
     */
    @FXML void reviewStopBtnPressed(ActionEvent event) {
        reviewMediaPlayer.stop();
    }

    /**
     * Confirms whether the user wants to delete the video file and removes their selection upon confirmation
     * @param event
     */
    @FXML void reviewDeleteBtnPressed(ActionEvent event) {
        Creation selection = tableViewForReview.getSelectionModel().getSelectedItem();
        String videoName = selection.getName();
        try {
            if(selection == null){
                //do nothing
                return;
            }
            // Get confirmation from user for creation deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + videoName + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                genericProcess("rm " + _path + "/" + videoName + ".mp4 " + _path + "/creations.txt;touch " + _path + "/creations.txt");
                _existingCreations.remove(selection);

                saveCreations(_existingCreations);
                tableViewForReview.getItems().clear();
                tableViewForReview.getItems().setAll(_existingCreations);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Shows the user a pop up listing the instructions and details of the review scene
     * @throws Throwable
     */
    @FXML public void reviewHelpBtnPressed() throws Throwable {
        _alert = _alertGenerator.newAlert("Active Learning Component", "", "" +
                "1. Choose a creation that you want to play by clicking on the list\n\n" +
                "2. Press the big 'Play' button on the button to load the video\n\n" +
                "3. Under the video you will see three buttons 'Play, 'Pause' and 'Stop'\n\n" +
                "4. You can edit your level of confidence by clicking on 'Confidence Level' column (only from the range 1-5)\n\n" +
                "5. THe 'Number of plays' will only increase if you watch the whole video\n\n" +
                "6. You can click the headers of each column to sort your creations in order\n\n" +
                "7. We encourage you to create more videos if you have a lot of 4's and 5's in your 'Confidence Levels'", "information");
        _alert.show();
    }

    /**
     * Controls the logic of editing the table view cell: ConfidenceLevel.
     */
    public void editableConfidence() {
        _confidenceLevel.setCellFactory(TextFieldTableCell.forTableColumn());
        _confidenceLevel.setOnEditCommit(e -> {

            String input = e.getNewValue();

            if (input.matches("[0-9]+")) {
                if (Integer.parseInt(input) < 1 || Integer.parseInt(input) > 5) {
                    // show alert
                    try {
                        _alert = _alertGenerator.newAlert("Invalid Input", "Invalid number range", "Enter a number between 1 to 5 inclusively", "error");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    Optional<ButtonType> option = _alert.showAndWait();
                    if (option.get() == ButtonType.OK) {
                        tableViewForReview.getItems().setAll(this._existingCreations);
                    }
                } else {
                    // set the new value of confidence level
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setConfidence(e.getNewValue());
                    try {
                        saveCreations(_existingCreations);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    _alert = _alertGenerator.newAlert("Invalid Input", "Invalid number range", "Enter a number between 1 to 5 inclusively", "error");
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Optional<ButtonType> option = _alert.showAndWait();
                if (option.get() == ButtonType.OK) {
                    tableViewForReview.getItems().setAll(this._existingCreations);
                }
            }

        });

    }



    /**************************** END OF REVIEW TAB LOGIC *************************************/


    //--------------------------------- helper functions ---------------------------------------

    /**
     * Clears all text fields and removes all temporary files needed for creations
     * @throws IOException
     * @throws InterruptedException
     */
    public void clearAll() throws IOException, InterruptedException {
        searchField.clear();
        searchTextArea.clear();
        selectedTextArea.clear();
        _searchTerm = "";
        creationNameField.clear();
        String cmd = "rm -r " + _path + "/temp/ " + _path + "/*.wav";
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
        Process process = builder.start();
        process.waitFor();
        process.destroy();
        refreshAudioList();
    }

    /**
     * Counts the number of words in a string
     * @param str
     * @return
     */
    public int getWordCounts(String str) {
        int count=0;
        char ch[]= new char[str.length()];
        for(int i=0;i<str.length();i++)
        {
            ch[i]= str.charAt(i);
            if( ((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' ')) || ((ch[0]!=' ')&&(i==0)) )  {
                count++;

            }

        }
        return count;

    }


    /**
     * Updates the audio list in the audio creation scene
     */
    public void refreshAudioList() {
        List<String> fileNames = getAudioNameList();
        ObservableList<String> items = FXCollections.observableList(fileNames);
        audioCreationsList.setItems(items);
    }

    /**
     * Gets a list of existing audio files created by the user in the audio creation scene
     * @return
     */
    public List<String> getAudioNameList() {
        try {
            String cmd = "ls " + _path + "/" + " | grep wav | sort | cut -f1 -d'.'";
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
            Process process = builder.start();
            InputStream stdout = process.getInputStream();
            BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
            List<String> fileNames = new ArrayList<String>();
            String line = null;
            while ((line = stdoutBuffered.readLine()) != null) {
                fileNames.add(line);
            }
            process.waitFor();
            process.destroy();
            return fileNames;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }


    /**
     * Checks whether a file is valid/already exists
     * @param InputName
     * @return
     */
    public boolean isValidName(String InputName) {
        boolean IsExist = false;
        List<String> fileNames = getAudioNameList();
        for (int i = 0; i < fileNames.size(); i++) {
            if (InputName.equals(fileNames.get(i))) {
                IsExist = true;
                break;
            }
        }
        if (IsExist == false) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Checks whether a file already exists
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean isCreated() throws IOException, InterruptedException {
        String cmd = "cat " + _path + "/" + "error.txt";
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
        Process process = pb.start();
        InputStream stdout = process.getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String line = stdoutBuffered.readLine();
        String temp = "SIOD ERROR: could not open file /usr/share/festival/dicts/oald/oald_lts_rules.scm";
        process.waitFor();
        process.destroy();

        if (line == null) {
            return true;
        } else if (line.equals(temp)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Generates the voice files used for audio creation in the audio creation scene
     * @throws IOException
     */
    public void generateScm() throws IOException {
        FileWriter fw1 = new FileWriter(_path + "/" + "akl.scm");
        fw1.write("(voice_akl_nz_jdt_diphone)");
        fw1.close();

        FileWriter fw2 = new FileWriter(_path + "/" + "kal.scm");
        fw2.write("(voice_kal_diphone)");
        fw2.close();

    }

    /**
     * Gets the number of selected audio files by the user in the audio creation scene
     * @return
     */
    public int getSelectedAudioCreationsCount() {
        ObservableList<String> selectedItems = audioCreationsList.getSelectionModel().getSelectedItems();
        _audioFileName = "";
        int count = 0;
        // must be multiple selection
        for (String s : selectedItems) {
            _audioFileName = _audioFileName + _path + "/" + s + ".wav ";
            count++;
        }
        return count;
    }


    /**
     * Updates the images for the toggle buttons  in the flickr/video creation scene when the flickr search is complete
     */
    public void updateImages() {
        File imagesDir = new File(_path + "/temp");
        File[] imagesList = imagesDir.listFiles();
        for(int i = 0; i < images.length; i++){
            Image image = new Image(imagesList[i].toURI().toString());
            images[i].setImage(image);
        }
    }

    /**
     * Gets the list of selected images by the user in the flickr/video creation scene
     * @return
     */
    public List<Image> getSelectedImages() {
        int count = 0;
        //Initial check if no images are selected
        for(int i = 0; i < imageButtons.length; i++) {
            if(imageButtons[i].isSelected()){
                count++;
            }
        }
        if(count == 0){
            return null;
        }
        List<Image> selectedImages = new ArrayList();
        //There is at least one image selected so run the loop
        for(int i  = 0; i < imageButtons.length; i++) {
            if(imageButtons[i].isSelected()){
                selectedImages.add(images[i].getImage());
            }
        }
        return selectedImages;
    }

    /**
     * Gets the list of unselected images so they can be deleted in preparation for video creation in the flickr/video
     * creation scene
     * @return
     */
    public List<Image> getUnselectedImages() {
        int count = 0;
        //Initial check if no images are selected
        for(int i = 0; i < imageButtons.length; i++) {
            if(imageButtons[i].isSelected()){
                count++;
            }
        }
        if(count == 0){
            return null;
        }
        List<Image> unselectedImages = new ArrayList();
        //There is at least one image selected so run the loop
        for(int i  = 0; i < imageButtons.length; i++) {
            if(!imageButtons[i].isSelected()){
                unselectedImages.add(images[i].getImage());
            }
        }
        return unselectedImages;
    }

    /**
     * Removes all the unselected images in preparation for video creation in the flickr/video creation scene
     * @throws IOException
     */
    public void removeUnselectedImages() throws IOException {
        List<Image> unselectedImages = getUnselectedImages();
        for(int i = 0; i < unselectedImages.size(); i++){
            String jpgPath =  unselectedImages.get(i).impl_getUrl();
            System.out.println(jpgPath.substring(5));
            File file = new File(jpgPath.substring(5));
            file.delete();
        }
    }

    /**
     * Saves the existing creation objects to a text file so data is saved when application is closed
     * @param creations
     * @throws IOException
     */
    public void saveCreations(List<Creation> creations) throws IOException {
        File file = new File(_path + "/creations.txt");
        file.delete();

        FileOutputStream fileOutput = new FileOutputStream(file);
        ObjectOutputStream objOutput = new ObjectOutputStream(fileOutput);

        //Save and write the creation objs to a txt file
        for (Creation c:creations){
            objOutput.writeObject(c);
        }

        fileOutput.close();
        objOutput.close();
    }

    /**
     * Used for reading existing creation objects created before previous shutdowns so state is saved for existing creations
     * @throws IOException
     */
    public void updateCreations() throws IOException {
        File file = new File(_path + "/creations.txt");

        try {
            FileInputStream fileInput = new FileInputStream(file);
            ObjectInputStream objInput = new ObjectInputStream(fileInput);

            boolean end = false;
            while (!end) {
                Creation c = (Creation) objInput.readObject();
                if (c != null) {
                    _existingCreations.add(c);
                } else {
                    end = true;
                }
            }
            tableViewForReview.getItems().clear();
            tableViewForReview.getItems().addAll(_existingCreations);

            fileInput.close();
            objInput.close();
        }
        catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (EOFException e){
            //Expected output
        }

    }

    /**
     * Checks whether input string is alphanumeric - handles unicode
     * @param str
     * @return
     */
    public boolean isAlphanumeric2(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
                return false;
        }
        return true;
    }

    /**
     * Checks whether input string is alphanumeric
     * @param str
     * @return
     */
    public boolean isAlphanumeric(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c))
                return false;
        }

        return true;
    }

    /**
     * Runs a process given a bash command, waits for the process to finish then destroys it. This is only used for commands
     * that do not need to reference the process outputs and used only for processes that are very quick.
     * @param cmd
     * @throws InterruptedException
     * @throws IOException
     */
    public void genericProcess(String cmd) throws InterruptedException, IOException {
        ProcessBuilder genericPb = new ProcessBuilder("bash", "-c" , cmd);
        Process genericProcess = genericPb.start();
        genericProcess.waitFor();
        genericProcess.destroy();;
    }

    /**
     * Updates the GUI in the search scene while the search is running
     */
    public void setUpSearchWikiOnRunning(){
        searchCancelBtn.setVisible(true);
        searchProgress.setVisible(true);
        searchingLabel.setText("Searching please wait...");
        // disable the button for creation
        searchBtn.setDisable(true);
        searchingLabel.setVisible(true);
    }

    /**
     * Updates the GUi in the search scene when the search is cancelled
     */
    public void setUpSearchWikiOnCancelled(){
        searchField.clear();
        _searchTerm = "";
        searchCancelBtn.setVisible(false);
        searchProgress.setVisible(false);
        searchingLabel.setText("Search interrupted, please try again");
        searchBtn.setDisable(false);
        searchProgress.progressProperty().unbind();
        searchProgress.setProgress(0);
    }

    /**
     * Updates the GUI in the search scene when the search is completed
     */
    public void setUpSearchWikiOnSuccess(){
        searchCancelBtn.setVisible(false);
        searchProgress.setVisible(false);
        searchingLabel.setText("Search is complete");
        searchBtn.setDisable(false);
        searchProgress.progressProperty().unbind();
        searchProgress.setProgress(0);
    }

    /**
     * Runs a process in the search scene to check whether the output from the wikit command returns no results for the
     * users search term
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getSearchResultProcess() throws IOException, InterruptedException {
        String cmd = "cat " + _path + "/" + "textFromWiki.txt";
        ProcessBuilder searchWikiPb = new ProcessBuilder("bash", "-c", cmd);
        Process searchWikiProcess = searchWikiPb.start();
        InputStream searchWikiStdout = searchWikiProcess.getInputStream();
        BufferedReader searchWikiStdoutBuffered = new BufferedReader(new InputStreamReader(searchWikiStdout));
        String searchWikiLine = searchWikiStdoutBuffered.readLine();
        searchWikiProcess.waitFor();
        searchWikiProcess.destroy();

        return searchWikiLine;
    }

    /**
     * Runs a process in the search scene that updates the GUI with the users search results
     */
    public void setUpSearchResult(){
        try {
            String getTextCmd = "cat " + _path + "/" + "textFromWiki.txt";
            ProcessBuilder getTextPb = new ProcessBuilder("bash", "-c", getTextCmd);
            Process getTextProcess = getTextPb.start();
            InputStream getTextStdout = getTextProcess.getInputStream();
            BufferedReader getTextStdoutBuffered = new BufferedReader(new InputStreamReader(getTextStdout));
            String getTextLine = null;
            _searchText = "";
            while ((getTextLine = getTextStdoutBuffered.readLine()) != null) {
                _searchText = _searchText + getTextLine + "\n";
            }
            getTextProcess.waitFor();
            getTextProcess.destroy();
        } catch (IOException | InterruptedException getTextException) {
            getTextException.printStackTrace();
        }
        searchTextArea.setText(_searchText);
        _searchText = "";
    }

    /**
     * Updates the GUI in the flickr/video creation scene while the flickr search is running
     */
    public void setUpFlickrOnRunning(){
        for(int i = 0; i < images.length; i++){
            images[i].setVisible(false);
        }
        flickrSceneLabel.setText("Searching for images, please wait...");
        flickrProgress.progressProperty().bind(_flickrTask.progressProperty());

    }

    /**
     * Updates the GUI in the flickr/video creation scene when the flickr search is completed
     */
    public void setUpFlickrOnSuccess(){
        _flickrService.shutdown();
        for(int i = 0; i < images.length; i++){
            images[i].setVisible(true);
        }
        flickrSceneLabel.setText("Please click on the images that you would like in your creation");
        flickrProgress.progressProperty().unbind();
        flickrProgress.setVisible(false);
        finishFlickrBtn.setDisable(false);
        searchingLabel.setText("");
        updateImages();
    }

    /**
     * Updates the GUI in the flickr/video creation scene while the video creation task is running
     */
    public void setUpVideoCreationOnRunning(){
        flickrProgress.setVisible(true);
        flickrProgress.progressProperty().bind(_videoTask.progressProperty());
        flickrSceneLabel.setText("Generating video please wait...");
    }

    /**
     * Updates the GUI in the flickr/video creation scene when the video creation task is successful
     */
    public void setUpVideoCreationOnSuccess(){
        flickrProgress.setVisible(false);
        flickrProgress.progressProperty().unbind();
        flickrSceneLabel.setText("Video creation is complete");

        // Create a new creation obj for the video and update the tableview
        Creation creation = new Creation(creationNameField.getText(), _searchTerm);
        _existingCreations.add(creation);
        tableViewForReview.getItems().clear();
        tableViewForReview.getItems().addAll(_existingCreations);

        // Saves the creations to a text file to save state when application closes
        try {
            saveCreations(_existingCreations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Upon completion, notify the user and take them back to the search scene
        try {
            _alert = _alertGenerator.newAlert("Creation success", "Video generation successful", "Click 'Ok' to return to the beginning", "information");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        _alert.showAndWait();
        searchTextPane.toFront();
        try {
            clearAll();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}



