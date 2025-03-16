import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;

/**
 * A tone takes an audio format
 */
public class Tone {

    // map of the tones that we create from a song file
    private final Map<String, Note> toneMap = Map.ofEntries(
            Map.entry("A4", Note.A4), Map.entry("A4S", Note.A4S),
            Map.entry("B4", Note.B4), Map.entry("C4", Note.C4),
            Map.entry("C4S", Note.C4S), Map.entry("D4", Note.D4),
            Map.entry("D4S", Note.D4S), Map.entry("E4", Note.E4),
            Map.entry("F4", Note.F4), Map.entry("F4S", Note.F4S),
            Map.entry("G4", Note.G4), Map.entry("G4S", Note.G4S),
            Map.entry("A5", Note.A5), Map.entry("REST", Note.REST));

    // map of the lengths that we create from a song file
    private final Map<String, NoteLength> lengthMap = Map.ofEntries(
            Map.entry("1", NoteLength.WHOLE), Map.entry("2", NoteLength.HALF),
            Map.entry("4", NoteLength.QUARTER), Map.entry("8", NoteLength.EIGHTH));

    // the tone's audio format
    private final AudioFormat af;

    /**
     * Loads and creates a list of notes from a specified song file
     *
     * @param filename the name of the song file to load
     * @return the loaded song
     */
    public List<BellNote> loadSong(String filename) throws FileNotFoundException {

        // print all the errors we encounter -- if it is invalid in any way then don't return the song
        boolean invalidSong = false;

        // load the song from a file
        List<BellNote> song = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {

                // is it separated by spaces?
                try {
                    String[] note = scanner.nextLine().split(" ");

                    // valid line
                    if (note.length == 2) {
                        Note n = toneMap.get(note[0]);
                        NoteLength length = lengthMap.get(note[1]);

                        // we were not given a valid note -- return a null song
                        if (n == null) {
                            System.err.println("Invalid Note: " + note[0]);
                            invalidSong = true;
                        }

                        // we were not given a valid length -- return a null song
                        if (length == null) {
                            System.err.println("Invalid Note Length: " + note[1]);
                            invalidSong = true;
                        }

                        song.add(new BellNote(n, length));
                    } else {
                        System.err.println("Invalid Line -- lines should contain two values (Note NoteLength) separated by space");
                        invalidSong = true;
                    }
                } catch (Exception e) {
                    invalidSong = true;
                    System.err.println(e);
                }
            }
        }

        // there was 1+ errors -- this is an invalid song
        if (invalidSong) {
            return null;
        }
        System.out.println(filename + " is a valid song file");
        return song;
    }

    /**
     * The tone of the note depends on our audio format
     * @param af audioFormat of the note
     */
    Tone(AudioFormat af) {
        this.af = af;
    }
}

/**
 * A bell note has a note and the length to play the note for
 */
class BellNote {
    final Note note;
    final NoteLength length;

    /**
     * Create a bell note
     *
     * @param note   the note to play
     * @param length how long to play the note
     */
    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }

    @Override
    public String toString() {
        return note + " " + length;
    }
}

/**
 * Valid NoteLengths are whole, half, quarter and eighth
 */
enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGHTH(0.125f);

    private final int timeMs;

    /**
     * How long should we play the note for?
     * @param length the length of the note to be played
     */
    NoteLength(float length) {
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    /**
     * Returns the time of the note to be played in milliseconds
     * @return time
     */
    public int timeMs() {
        return timeMs;
    }
}

/**
 * These are all the valid notes we can play and what audio output they correspond to
 */
enum Note {
    // REST Must be the first 'Note'
    REST,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final int MEASURE_LENGTH_SEC = 1;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 127.0d;

    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    /**
     * Do fancy math to create the note's tone
     */
    Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    /**
     * Returns a sample of the note
     * @return byte array of the note
     */
    public byte[] sample() {
        return sinSample;
    }
}