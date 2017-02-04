package checkerinterface;

import checker.*;
import sidemodules.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

/*
 *  Offerings Checker by Deniz Ulusel
 *
 *  Simple Bilkent offerings checker with sound and e-mail notifications and logging
 *  To build and use, see README
 *
 *  Uses JavaFX for ui
 *  Uses Jsoup for http requests and html parsing:
 *  https://jsoup.org
 *
 *  Uses JavaMail for mail notifications:
 *  http://www.oracle.com/technetwork/java/javamail/index.html
 *
 *  ideas:
 *  Constructor is pretty messy. Separate individual scenes into classes?
 *  Doesn't work unless bilkent.edu.tr certificate is added to cacerts, add module to do it programmatically?
 *  Add tooltips to clarify input boxes?
 */

public class CheckerInterface extends Application
{
    static final int CHECK_DELAY = 200;
    static final int SOUND_DELAY = 30000;
    static final int MAIL_DELAY = 30000;
    static final int MAX_LINES = 300;

    Stage window;

    GridPane grid;
    ChoiceBox<String> deptBox, seasonBox;
    TextField courseNoField, sectionField, yearField, mailField;
    CheckBox playSoundBox, logToTxtBox, mailAlertBox;
    VBox mailFieldGroup;
    Button checkBtn;

    ConsoleArea output;
    BlinkingLight blink;
    CheckButton btn;

    boolean playSound, logToTxt, mailAlert;
    OfferingsChecker check;
    TxtLogger logger;
    SoundPlayer soundPlayer;
    MailBot mailBot;
    TimerTask checkTask;
    Timer timer;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        // name the stage "window", less confusing
        window = primaryStage;
        window.setTitle( "Offerings Checker");

        // set timer as daemon so JVM quits on exit
        // could alternatively kill the thread manually on exit
        timer = new Timer( true);

        // FIRST SCENE
        grid = new GridPane();
        grid.setPadding( new Insets( 10, 10, 10, 10)); // edges of gridPane
        grid.setVgap( 8);  // gaps between cells in grid
        grid.setHgap( 10);

        // First row
        Label classLabel = new Label( "Class: ");
        GridPane.setConstraints( classLabel, 0, 0);

        deptBox = new ChoiceBox<>();
        // probably shouldn't be hardcoded
        deptBox.getItems().addAll( "ACC", "ADA", "AMER", "ARCH", "BF", "BIM", "BTE", "CAA", "CAD", "CHEM", "CI", "CINT",
                "CITE", "COMD", "CS", "CTE", "CTIS", "CTP", "DIR", "ECON", "EDEB", "EE", "EEE", "EEPS", "ELIT", "ELS", "EM",
                "EMBA", "ENG", "ETE", "ETS", "FA", "FRE", "FRL", "FRP", "GE", "GER", "GIA", "GRA", "HART", "HCIV", "HIST",
                "HISTR", "HUM", "IAED", "IE", "IR", "ITA", "JAP", "LAUD", "LAW", "MAN", "MATH", "MBA", "MBG", "ME", "MIAPP",
                "MSC", "MSN", "MTE", "MUS", "MUSS", "NSC", "PE", "PHIL", "PHYS", "PNT", "POLS", "PREP", "PSYC", "RUS", "SFL",
                "SOC", "SPA", "TE", "TEFL", "THEA", "THM", "THR", "THS", "TRIN", "TRK", "TTP", "TURK");
        deptBox.setValue( "ACC");
        GridPane.setConstraints( deptBox, 1, 0);

        courseNoField = new TextField();
        GridPane.setConstraints( courseNoField, 2, 0);
        courseNoField.setPromptText( "Code");
        courseNoField.setPrefWidth( 50);
        addTextLimiter( courseNoField, 3);
        makeTextFieldNumeric( courseNoField);

        sectionField = new TextField();
        GridPane.setConstraints( sectionField, 3, 0);
        sectionField.setPromptText( "Section");
        sectionField.setPrefWidth( 63);
        addTextLimiter( sectionField, 2);
        makeTextFieldNumeric( sectionField);

        // Second row
        Label semesterLabel = new Label( "Semester: ");
        GridPane.setConstraints( semesterLabel, 0, 1);

        yearField = new TextField();
        GridPane.setConstraints( yearField, 1, 1);
        yearField.setPromptText( "Year");
        yearField.setPrefWidth(1);
        addTextLimiter( yearField, 4);
        makeTextFieldNumeric( yearField);

        seasonBox = new ChoiceBox<>();
        seasonBox.getItems().addAll( "Fall", "Spring", "Summer");
        seasonBox.setValue( "Fall");
        GridPane.setConstraints( seasonBox, 2, 1, 2, 1); // 3rd and 4th parameters for cell span

        playSoundBox = new CheckBox( "Play sound");
        GridPane.setConstraints( playSoundBox, 1, 2, 2, 1);
        logToTxtBox = new CheckBox( "Log output to txt");
        GridPane.setConstraints( logToTxtBox, 1, 3, 2, 1);

        mailAlertBox = new CheckBox( "Send e-mail notification");
        mailField = new TextField();
        mailField.setPromptText( "e-mail address");
        mailFieldGroup = new VBox( 8);
        mailFieldGroup.getChildren().add( mailAlertBox);
        mailAlertBox.setOnAction( e -> {
            if ( mailAlertBox.isSelected())
                mailFieldGroup.getChildren().add( mailField);
            else
                mailFieldGroup.getChildren().remove( mailField);

            // adjusts window size when the box is added/removed
            window.sizeToScene();
        });

        GridPane.setConstraints( mailFieldGroup, 1, 4, 2, 1);

        checkBtn = new Button( "CHECK");
        GridPane.setConstraints( checkBtn, 1, 5);

        // Final calls
        grid.getChildren().addAll( courseNoField, classLabel, yearField, semesterLabel, checkBtn, deptBox, sectionField,
                seasonBox, playSoundBox, logToTxtBox, mailFieldGroup);
        Scene firstScene = new Scene( grid); // firstScene and secondScene, really?

        // SECOND SCENE
        output = new ConsoleArea();
        blink = new BlinkingLight();
        btn = new CheckButton( "FORCE CHECK", 1000); // manual request button is redundant(?), vestige of testing
        VBox layout = new VBox( 10);
        HBox bottomBar = new HBox( 10);
        StackPane pane = new StackPane();
        pane.getChildren().add( blink);
        bottomBar.getChildren().addAll( btn, pane);
        layout.setPadding( new Insets( 20, 20, 20, 20));
        layout.getChildren().addAll( output, bottomBar);
        pane.setAlignment( Pos.CENTER_LEFT);
        btn.setAlignment( Pos.CENTER_RIGHT);

        Scene secondScene = new Scene( layout);

        checkBtn.setOnAction( e -> {
            window.setScene( secondScene);
            setUpChecker();
            });

        // flush logger's buffer on exit
        window.setOnCloseRequest( e -> {
            if ( logToTxt)
                logger.close();
        });

        window.setResizable( false);

        window.setScene( firstScene);
        window.show();
    }

    private void setUpChecker()
    {
        int season;

        if ( seasonBox.getValue().equals( "Fall"))
            season = 1;
        else if ( seasonBox.getValue().equals( "Spring"))
            season = 2;
        else
            season = 3;

        check = new OfferingsChecker( deptBox.getValue().toString(), Integer.parseInt( courseNoField.getText()), Integer.parseInt( sectionField.getText()),
                Integer.parseInt( yearField.getText()), season);

        // trash first request
        requestQuota( false);

        checkTask = new TimerTask() {
            @Override
            public void run()
            {
                requestQuota( true);
            }
        };

        timer.schedule( checkTask, 0, CHECK_DELAY);

        playSound = playSoundBox.isSelected();
        logToTxt = logToTxtBox.isSelected();
        mailAlert = mailAlertBox.isSelected();

        // storing as boolean as these are checked on every request
        if ( playSound)
            soundPlayer = new SoundPlayer("alert.wav", SOUND_DELAY);

        if ( logToTxt)
            logger = new TxtLogger( check.getCourseLabel() + ".txt");

        if ( mailAlert)
            mailBot = new MailBot( MAIL_DELAY, mailField.getText());
    }

    public static String generateTimeStamp()
    {
        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp( date.getTime());
        return "[" + String.format("%1$TF %1$TT", timestamp) + "] ";
    }

    public String reportQuota( int quota)
    {
        return generateTimeStamp() + check.getCourseLabel() + " quota: " + quota;
    }

    public String reportError( String error)
    {
        return generateTimeStamp() + "ERROR: " + error;
    }

    // probably not the best way to do this,
    // there MUST be a better way to do if ( visible)
    private void requestQuota( boolean visible)
    {
        String message;

        try {
            int quota = check.getCurrentQuota();
            if ( visible)
            {
                blink.blinkGreen();
                message = reportQuota( quota);
                output.write( message);

                if ( playSound && quota > 0)
                    soundPlayer.run();

                if ( logToTxt)
                {
                    logger.writeLine( message);
                }

                if ( mailAlert && quota > 0)
                {
                    mailBot.sendMail( "Offerings Checker Notification", message);
                }
            }
        } catch (NullPointerException e) {
            if ( visible)
            {
                blink.blinkRed();
                message = reportError("Bad input");
                output.write( message);

                if ( logToTxt)
                    logger.writeLine( message);
            }
        } catch (SocketTimeoutException e) {
            if ( visible)
            {
                blink.blinkRed();
                message = reportError("Read timed out");
                output.write( message);

                if ( logToTxt)
                    logger.writeLine( message);
            }
        } catch (SSLHandshakeException e) {
            if ( visible)
            {
                blink.blinkRed();
                message = reportError("Bilkent certificate not trusted");
                output.write( message);

                if ( logToTxt)
                    logger.writeLine( message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ( visible)
            {
                blink.blinkRed();
                message = reportError( e.getClass().getSimpleName());
                output.write( message);

                if ( logToTxt)
                    logger.writeLine( message);
            }
        }
    }

    /* adds character limit to given text box. see:
     * http://stackoverflow.com/questions/15159988/javafx-2-2-textfield-maxlength
     */
    public static void addTextLimiter( TextField tf, int maxLength) {
        tf.textProperty().addListener( new ChangeListener<String>() {
            @Override
            public void changed( final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if ( tf.getText().length() > maxLength) {
                    String s = tf.getText().substring( 0, maxLength);
                    tf.setText( s);
                }
            }
        });
    }

    /*
     * allows only numeric characters in a given text box. see:
     * http://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
     */
    public static void makeTextFieldNumeric( TextField tf)
    {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed( ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    // uneditable text area
    class ConsoleArea extends TextArea
    {
        public ConsoleArea()
        {
            super();
            setPrefWidth( 350);
            setPrefHeight( 197);
            setEditable(false);
        }

        /*
         * Method override to limit number of lines in console
         * Source is stackoverflow, can't seem to find the link
         */
        @Override
        public void replaceText(int start, int end, String text) {
            super.replaceText( start, end, text);
            while( getText().split( "\n", -1).length > MAX_LINES)
            {
                int fle = getText().indexOf( "\n");
                super.replaceText( 0, fle + 1, "");
            }
            positionCaret( getText().length());
        }

        void write( String message)
        {
            /*
             * Bugfix for TextArea exceptions, see:
             * http://stackoverflow.com/questions/30863862/javafx-append-text-to-textarea-throws-exception
             */
            javafx.application.Platform.runLater( () -> appendText( message + "\n") );
        }
    }

    // experimental blinking light thing
    class BlinkingLight extends Circle
    {
        Timeline greenBlink, redBlink;

        public BlinkingLight()
        {
            super(0, 0, 10, Color.YELLOW);
            setStrokeWidth(2);

            // set lighting effect
            Light.Distant light = new Light.Distant();
            light.setAzimuth(-135.0);
            Lighting lighting = new Lighting();
            lighting.setLight(light);
            lighting.setSurfaceScale(5.0);
            setEffect( lighting);

            // define blink animations
            greenBlink = new Timeline(
                    new KeyFrame(Duration.seconds(0.0), e -> setFill(Color.GREEN)),
                    new KeyFrame(Duration.seconds(0.5), e -> setFill(Color.YELLOW))
            );
            greenBlink.setCycleCount(1);

            redBlink = new Timeline(
                    new KeyFrame(Duration.seconds(0.0), e -> setFill(Color.ORANGERED)),
                    new KeyFrame(Duration.seconds(0.5), e -> setFill(Color.YELLOW))
            );
            redBlink.setCycleCount(1);
        }

        void blinkGreen()
        {
            greenBlink.play();
        }

        void blinkRed()
        {
            redBlink.play();
        }
    }

    // jamming button to prevent excessive http requests
    class CheckButton extends Button
    {
        public CheckButton( String label, int delay)
        {
            super( label);

            Timeline jam = new Timeline(new KeyFrame( Duration.millis( delay), e -> setDisable( false)));
            jam.setCycleCount(1);

            setOnAction( e -> {
                setDisable( true);
                requestQuota( true);
                jam.play();
            });
        }
    }
}