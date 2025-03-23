import javax.sound.sampled.SourceDataLine;

/**
 * A member of the bell choir!
 * Members have one bell that they can either play or not play
 * Members play the bell one at a time, so each member's thread is synchronized,
 * meaning when one member plays it will continue to play its' note until it is
 * done then the next member will start playing
 */
public class Member implements Runnable {
    // takes and plays 1 note
    private String name = ""; // adds no functionality -- but I think players should have names
    private final Note note; // one note (bell) in their hand
    private NoteLength noteLength; // how long should we play this note for
    private final Thread thread; // the member's thread
    public volatile boolean running; // is the member "belling"?

    /**
     * Constructs a member, with a name, note and the choir's audio format
     * *Note that a member's bell note contains an arbitrary note duration, but the
     * duration will change throughout the song
     *
     * @param name member's name
     * @param note member's bell they will play throughout the song(s)
     */
    public Member(String name, Note note) {
        this.name = name;
        this.note = note;
        this.noteLength = null;
        this.thread = new Thread(this, name);
    }

    public void startBelling() {
        thread.start();
    }

    /**
     * Plays the Member's note for the specified duration
     * 
     * @param n
     * @param line
     */
    public void bellTime(NoteLength n, SourceDataLine line) {
        System.out.println(name + " on the... bells: " + note);
        this.noteLength = n;
        final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 5);
    }

    /**
     * Stops playing the note
     */
    public void stopBelling() {
        running = false;
    }

    /**
     * Joins the thread
     */
    public void joinBells() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the thread which will play the member's note
     */
    @Override
    public void run() {
    }
}
