import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conductor {



    // arbitrary names that do not affect the functionality of the program
    private final String[] names = new String[]{
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

    private final AudioFormat af;
    private List<BellNote> song;
    private HashMap<Note, Member> choir;

    public Conductor(List<BellNote> song, AudioFormat af) {
        this.song = song;
        this.af = af;
        recruitChoir();
    }

    public void playSong() {
        if (song == null) {
            System.out.println("Invalid Song");
        } else {

            for (BellNote bn : song) {

                Member m = choir.get(bn.note);
                try {
                    m.startBelling(bn.length);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            for (Member m : choir.values()) {
                m.stopBelling();
            }

            for (Member m : choir.values()) {
                try {
                    m.joinBells();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Recruits members of the choir by adding them to a hash map
     * When we want to play a song we will get the note of the member and have them
     * play
     *
     * @return
     */
    private void recruitChoir() {
        // save choir members in a hash map
        choir = new HashMap<>();
        int nameIndex = 0;
        // for each tone that could be played, make a member to play it!
        for (String key : toneMap.keySet()) {
            choir.put(toneMap.get(key), new Member(names[nameIndex], toneMap.get(key), af));
            nameIndex++;
        }
    }

    public void changeSong(List<BellNote> song){
        this.song = song;
    }

}