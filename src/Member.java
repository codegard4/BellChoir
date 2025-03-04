import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A member of the bell choir!
 * Members have one bell that they can either play or not play
 * Members play the bell one at a time, so each member's thread is synchronized,
 * meaning when one member plays it will continue to play its' note until it is
 * done
 * then the next member will start playing
 */
public class Member implements Runnable {
    // takes and plays 1 or 2 notes
    private String name = ""; // adds no functionality -- but I think players should have names
    private final Note note; // one note in their left hand
    private final Thread thread;
    private final AudioFormat audioFormat;

    /**
     * Constructs a member, with a name, note and the choir's audio format
     * *Note that a member's bell note contains an arbitrary note duration, but the
     * duration will change throughout the song
     * 
     * @param name
     * @param note
     * @param audioFormat
     */
    public Member(String name, Note note, AudioFormat audioFormat) {
        this.name = name;
        this.note = note;
        this.thread = new Thread(this);
        this.audioFormat = audioFormat;
    }

    /**
     * Plays the Member's note for the specified duration
     * 
     * @param n
     * @throws LineUnavailableException
     */
    public synchronized void startBelling(NoteLength n) throws LineUnavailableException {
        System.out.println(name + " on the...er... bells: " + note);
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat)) {
            line.open();
            line.start();
            final int ms = Math.min(n.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
            final int length = Note.SAMPLE_RATE * ms / 1000;
            line.write(note.sample(), 0, length);
            line.write(Note.REST.sample(), 0, 5);
            line.drain();
        } catch (LineUnavailableException ignore) {
        }
    }

    /**
     * Stops playing a note
     */
    public void stopBelling() {
        thread.interrupt();
    }

    /**
     * Joins the thread
     * 
     * @throws InterruptedException
     */
    public void joinBells() throws InterruptedException {
        thread.join();
    }

    /**
     * Returns the note the member has
     * 
     * @return
     */
    public Note myBell() {
        return note;
    }

    @Override
    /**
     * Runs the thread which will play the member's note
     */
    public void run() {
        // play the notes
        if (hasNote()) {
            thread.start();
            System.out.println("Mom I got a bell!!! " + name + " GOT A BELLLLLLL!!! ");
        } else {
            System.out.println(name + " is not good enough yet to get a bell");
        }

    }

    /**
     * Check if the member has been given a bell to play!
     * 
     * @return
     */
    public boolean hasNote() {
        return note != null;
    }

}
