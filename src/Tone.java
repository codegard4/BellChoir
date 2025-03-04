import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

public class Tone {

    // arbitrary names that do not affect the functionality of the program
    private final String[] names = new String[] {
            "Ted", "Shaun", "Murat", "Molly", "Charlie", "Jack", "Abe", "Andrew", "Justin", "Fuzzy", "Nate", "Pink",
            "Cole", "Slippers"
    };

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
            Map.entry("4", NoteLength.QUARTER), Map.entry("8", NoteLength.EIGTH));

    /**
     * Loads and creates a list of notes from a specified song file
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
    private List<BellNote> loadSong(String filename) throws FileNotFoundException {
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
                            System.out.println("Invalid Note: " + note[0]);
                            return null;
                        }

                        // we were not given a valid length -- return a null song
                        if (length == null) {
                            System.out.println("Invalid Note Length: " + note[1]);
                            return null;
                        }

                        song.add(new BellNote(n, length));
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
        return song;
    }

    /**
     * Recruits members of the choir by adding them to a hash map
     * When we want to play a song we will get the note of the member and have them
     * play
     * 
     * @return
     */
    private HashMap<Note, Member> recruitChoir() {
        // save choir members in a hash map
        HashMap<Note, Member> bellChoir = new HashMap<>();
        int nameIndex = 0;
        // for each tone that could be played, make a member to play it!
        for (String key : toneMap.keySet()) {
            bellChoir.put(toneMap.get(key), new Member(names[nameIndex], toneMap.get(key), af));
            nameIndex++;
        }
        return bellChoir;
    }

    public static void main(String[] args) throws Exception {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);
        List<BellNote> song = t.loadSong("songs/badsong.txt");

        // add the members of the choir!
        // *each member will start with an arbitrary note length which will change
        // throughout the song

        t.playSong(song);

    }

    private void playSong(List<BellNote> song) {
        if (song == null) {
            System.out.println("Invalid Song");
        } else {
            HashMap<Note, Member> bellChoir = recruitChoir();
            for (BellNote bn : song) {

                Member m = bellChoir.get(bn.note);
                try {
                    m.startBelling(bn.length);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            for (Member m : bellChoir.values()) {
                m.stopBelling();
            }

            for (Member m : bellChoir.values()) {
                try {
                    m.joinBells();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final AudioFormat af;

    Tone(AudioFormat af) {
        this.af = af;
    }
}

class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }

    @Override
    public String toString() {
        return note + " " + length;
    }
}

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

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

    private Note() {
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

    public byte[] sample() {
        return sinSample;
    }
}