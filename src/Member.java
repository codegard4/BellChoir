import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A member of the bell choir!
 * Members have one bell that they can either play or not play
 * Members play the bell one at a time, so each member's thread is synchronized,
 * meaning when one member plays it will continue to play its' note until it is
 * done then the next member will start playing
 */
public class Member implements Runnable {
    // takes and plays 1 or 2 notes
    private String name = ""; // adds no functionality -- but I think players should have names
    private final Note note; // one note in their left hand
    private NoteLength noteLength; // how long should we play this note for
    private final Thread thread; // the member's thread
    private final AudioFormat audioFormat; // the audio format to use
    public volatile boolean running; // is the member "belling"?

    /**
     * Constructs a member, with a name, note and the choir's audio format
     * *Note that a member's bell note contains an arbitrary note duration, but the
     * duration will change throughout the song
     *
     * @param name member's name
     * @param note member's bell they will play throughout the song(s)
     * @param audioFormat the audio format for the note to be played with
     */
    public Member(String name, Note note, AudioFormat audioFormat) {
        this.name = name;
        this.note = note;
        this.noteLength = null;
        this.thread = new Thread(this, name);
        this.audioFormat = audioFormat;
    }

    /**
     * Plays the Member's note for the specified duration
     */
    public synchronized void startBelling(NoteLength n) {
        System.out.println(name + " on the...er... bells: " + note);
        this.noteLength = n;
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat)) {
            line.open();
            line.start();
            final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
            final int length = Note.SAMPLE_RATE * ms / 1000;
            line.write(note.sample(), 0, length);
            line.write(Note.REST.sample(), 0, 5);
            line.drain();
        } catch (LineUnavailableException ignore) {
        }
    }


    /**
     * Stops playing the note
     */
    public void stopBelling() {
//        System.out.println("My moment of glory is over .. :/");
        running = false;
        thread.interrupt();
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
    public synchronized void run() {
        if (hasNote()) {
            System.out.println("Mom I got a bell!!! " + name + " GOT A BELLLLLLL!!! ");
        } else {
            System.out.println(name + " is not good enough yet to get a bell");
        }
    }

    /**
     * Check if the member has been given a bell to play!
     *
     * @return whether this member has a note
     */
    public boolean hasNote() {
        return note != null;
    }

    /**
     * Check if a member has a note length to play its note for
     *
     * @return whether the conductor told us how long to play for
     */
    public boolean hasNoteLength() {
        return noteLength != null;
    }

}
