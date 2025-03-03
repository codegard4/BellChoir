import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Member implements Runnable{
    // takes and plays 1 or 2 notes
    private String name = ""; // adds no functionality -- but I think players should have names
    private BellNote note; // one note in their left hand
    private Thread thread;
    private AudioFormat audioFormat;

    public Member(String name, BellNote note, AudioFormat audioFormat){
        this.name = name;
        this.note = note;
        this.thread = new Thread(this);
        this.audioFormat = audioFormat;
        thread.start();
    }

    public synchronized void startBelling() throws LineUnavailableException{
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat)) {
            line.open();
            line.start();
            final int ms = Math.min(note.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
            final int length = Note.SAMPLE_RATE * ms / 1000;
            line.write(note.note.sample(), 0, length);
            line.write(Note.REST.sample(), 0, 10);
            line.drain();
        }
    }

    public void stopBelling(){
        thread.interrupt();
    }

    public void joinBells() throws InterruptedException{
        thread.join();
    }
/* 
     void playSong(List<BellNote> song) throws LineUnavailableException {
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
       
    } */

    @Override
    public void run(){
        // play the notes
        if(hasNote()){
            System.out.println(name + " on the...er... bells: " + note);
        }
        else{
            System.out.println(name + " is not good enough yet to get a bell");
        }
        
    }
    public boolean hasNote(){
        return note != null;
    }

}
