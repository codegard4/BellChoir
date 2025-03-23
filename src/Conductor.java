import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A conductor controls members of the bell choir, telling them when they should
 * start and stop belling
 */
public class Conductor {

    public static void main(String[] args) throws Exception {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

        Tone t = new Tone(af);
        List<BellNote> seven = t.loadSong("songs/SevenNationArmy.txt");
        List<BellNote> mary = t.loadSong("songs/MaryHadALittleLamb.txt");
        List<BellNote> invalid = t.loadSong("songs/badsong.txt"); // won't work -- invalid note, space and line
        Conductor c = new Conductor(invalid, af);
        c.changeSong(seven); // plays seven nation army
        c.playSong();
        c.changeSong(mary); // plays mary had a little lamb
        c.playSong();
    }

    // arbitrary names that do not affect the functionality of the program
    private final String[] names = new String[] {
            "Ted", "Shaun", "Murat", "Molly", "Charlie", "Jack", "Abe", "Andrew", "Justin", "Kelly", "Nate", "Christi",
            "Cole", "Nick"
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

    // save the audio format -- that will not change across songs
    private final AudioFormat af;

    // save the current song but allow the conductor to play different songs
    private List<BellNote> song;

    // keep track of the choir
    private HashMap<Note, Member> choir;

    /**
     * A conductor has a song and audio format provided initially then recruits
     * their choir
     *
     * @param song the first song to play
     * @param af   the audio format to play all songs with
     */
    public Conductor(List<BellNote> song, AudioFormat af) {
        this.song = song;
        this.af = af;
        recruitChoir();
    }

    /**
     * Play a song -- tell the member to use their bell when it is their turn
     */
    public void playSong() {
        if (song == null) {
            System.out.println("Invalid Song");
        } else {
            try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                line.open();
                line.start();
                for (Member m : choir.values()) {
                    m.startBelling();
                }
                // play each note in the song one member belling at a time
                for (BellNote bn : song) {
                    Member m = choir.get(bn.note);
                    synchronized (m) {
                        m.bellTime(bn.length, line);

                    }
                }
                for (Member m : choir.values()) {
                    m.stopBelling();
                    m.joinBells();
                }

                line.drain();
            } catch (LineUnavailableException ignore) {
            }
        }
    }

    /**
     * Recruits members of the choir by adding them to a hash map
     * When we want to play a song we will get the note of the member and have them
     * play
     */
    private void recruitChoir() {

        // save choir members in a hash map
        choir = new HashMap<>();
        int nameIndex = 0;

        // for each tone that could be played, get a member to play it!
        for (String key : toneMap.keySet()) {
            choir.put(toneMap.get(key), new Member(names[nameIndex], toneMap.get(key)));
            nameIndex++;
        }
        System.out.println("Choir recruited!");
    }

    /**
     * Changes the song -- allows a conductor and choir to play multiple songs
     *
     * @param song new song to be played
     */
    public void changeSong(List<BellNote> song) {
        this.song = song;
    }

}