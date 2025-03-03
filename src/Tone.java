import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Tone {

    private Map<String, Note> toneMap = Map.ofEntries(
        Map.entry("A4", Note.A4),
        Map.entry("A4S", Note.A4S),
        Map.entry("B4", Note.B4),
        Map.entry("C4", Note.C4),
        Map.entry("C4S", Note.C4S),
        Map.entry("D4", Note.D4),
        Map.entry("D4S", Note.D4S),
        Map.entry("E4", Note.E4),
        Map.entry("F4", Note.F4),
        Map.entry("F4S", Note.F4S),
        Map.entry("G4", Note.G4),
        Map.entry("G4S", Note.G4S),
        Map.entry("A5", Note.A5),
        Map.entry("REST", Note.REST)
        );

    private Map<String, NoteLength> lengthMap = Map.ofEntries(
        Map.entry("1", NoteLength.WHOLE),
        Map.entry("2", NoteLength.HALF),
        Map.entry("4", NoteLength.QUARTER),
        Map.entry("8", NoteLength.EIGTH)
        );
    
    private List<BellNote> loadSong(String filename) throws FileNotFoundException{
        // load the song from a file
        List<BellNote> song = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filename));
        while(scanner.hasNextLine()){
            String[] note = scanner.nextLine().split(" ");
            // System.out.println(note[0] + " " + note[1]);
            song.add(new BellNote(toneMap.get(note[0]), lengthMap.get(note[1])));
        }
        scanner.close();
        return song;
    }

    public static void main(String[] args) throws Exception {
        final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        // Tone t = new Tone(af);
        // List<BellNote> song = t.loadSong("songs/SevenYears.txt");
        // t.playSong(song);
        List<Member> bellChoir = new ArrayList<>();
        bellChoir.add(new Member("Andrew", new BellNote(Note.A4, NoteLength.QUARTER), af));
        bellChoir.add(new Member("Abe", new BellNote(Note.B4, NoteLength.QUARTER), af));
        bellChoir.add(new Member("Charlie", new BellNote(Note.C4, NoteLength.QUARTER), af));
        bellChoir.add(new Member("Jack", new BellNote(Note.D4, NoteLength.QUARTER), af));
        bellChoir.add(new Member("Cole", new BellNote(Note.E4, NoteLength.QUARTER), af));

        for(Member member: bellChoir){
            member.startBelling();
        }
        for(Member member: bellChoir){
            member.joinBells();
        }
        for(Member member: bellChoir){
            member.stopBelling();
        }
    }

    private final AudioFormat af;

    Tone(AudioFormat af) {
        this.af = af;
    }

    public void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}

class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }
}

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
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
                sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}