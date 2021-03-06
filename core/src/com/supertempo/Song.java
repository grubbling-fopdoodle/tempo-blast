package com.supertempo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.supertempo.Resources.SongData;
import com.supertempo.Screens.Game.GameWorld;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Dominik on 6/21/2017.
 */

public class Song {

    static final float START_TIME = 3, NOTE_DELAY = 0f, NOTE_TOLERANCE = 0.25f;

    public String name_;
    ArrayList<Note> notes_;
    public Key[] keys_ = new Key[GameWorld.laneCount()];
    float noteLifeTime = 1f;
    int firstActiveNote_ = 0, lastActiveNote_ = 0;
    int firstVisibleNote_ = 0, lastVisibleNote_ = 0;
    float time_ = 0f;

    public SongData songData_;
    Music music_;
    boolean wasPlayed_ = false;
    boolean isPaused_ = false;

    //Scoring
    public int streak_, correct_, total_;

    public Song(SongData songData, Music music){

        for(int i = 0; i<keys_.length; i++){
            keys_[i] = new Key();
        }

        songData_ = songData;
        name_ = songData_.name();
        loadFromFile(songData.notePath());
        music_ = music;
    }

    //returns all active notes in reverse order
    public ArrayList<Note> activeNotes(){
        ArrayList<Note> result = new ArrayList<Note>();
        for(int i = lastActiveNote_ - 1; i>=firstActiveNote_; i--){
            result.add(notes_.get(i));
        }
        return result;
    }

    public ArrayList<Note> visibleNotes(){
        ArrayList<Note> result = new ArrayList<Note>();
        for(int i = lastVisibleNote_ - 1; i>=firstVisibleNote_; i--){
            //if(!notes_.get(i).wasPressed_) {
                result.add(notes_.get(i));
            //}
        }
        return result;
    }

    Note firstActiveNote(){
        return notes_.get(firstActiveNote_);
    }

    public void hitNote(int lane){
        hitNote(lane, false);
    }

    public void hitNote(int lane, boolean missed){
        boolean correct = false;
        if(!missed){
            ArrayList<Note> notes = activeNotes();
            for(Note note: notes){
                if(note.lane_ == lane){
                    note.wasPressed_ = true;
                    correct = true;
                    break;
                }
            }
        }

        //Updating scores
        if(correct){
            streak_++;
            correct_++;
        }
        else{
            streak_ = 0;
        }
        total_++;

        keys_[lane].click(correct);
    }

    public void updateTime(float time){

        //handle pausing
        if(isPaused_) return;

        playMusic();

        //maintaining list of hittable notes
        while(firstActiveNote_<notes_.size()  && notes_.get(firstActiveNote_).time_ < time_ - NOTE_TOLERANCE){
            if(!firstActiveNote().wasPressed_) {
                hitNote(firstActiveNote().lane_, true);
            }
            firstActiveNote_++;
        }
        while(lastActiveNote_ < notes_.size() - 1 && lastActiveNote_<notes_.size() && notes_.get(lastActiveNote_).time_ < time_ + NOTE_TOLERANCE){
            lastActiveNote_++;
        }
        for(int i = firstActiveNote_; i<=lastActiveNote_; i++) {
            notes_.get(i).activeValue_ = deltaToActiveValue(time_ - notes_.get(i).time_);
        }

        //maintaining list of visible notes
        while(firstVisibleNote_<notes_.size()  && notes_.get(firstVisibleNote_).time_ < time_ - NOTE_TOLERANCE){
            firstVisibleNote_++;
        }
        while(lastVisibleNote_ < notes_.size() - 1 && lastVisibleNote_<notes_.size() && notes_.get(lastVisibleNote_).time_ < time_ + noteLifeTime){
            lastVisibleNote_++;
        }
        for(int i = firstVisibleNote_; i<=lastVisibleNote_; i++) {
            notes_.get(i).value_ = (time_ - (notes_.get(i).time_ - noteLifeTime)) / (noteLifeTime + NOTE_TOLERANCE);
        }

        for(int i = 0; i<keys_.length; i++){
            keys_[i].updateDelta(time);
        }

        time_ += time;
    }

    public void randomize(int noteNo, int laneNo, float length){
        notes_ = new ArrayList<Note>(noteNo);
        for(int i = 0; i<noteNo; i++){
            int lane = ThreadLocalRandom.current().nextInt(0, laneNo);
            float time = (float)i * length/noteNo + 3;
            notes_.add(new Note(lane, time));
        }
    }

    public void loadFromFile(String notePath){

        notes_ = new ArrayList<Note>();
        FileHandle noteFile = Gdx.files.internal(notePath);
        Scanner scanner = new Scanner(noteFile.read());

        while(scanner.hasNext()){
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter("\\s*,\\s*");
            float time = Float.parseFloat(lineScanner.next())/2f+START_TIME+NOTE_DELAY;
            int lane = (int)Float.parseFloat(lineScanner.next())-1;
            notes_.add(new Note(lane, time));
        }
    }

    public float accuracy(){
        return (float)correct_ / total_;
    }

    public float playedRatio(){
        return time_ / songData_.length();
    }

    public int streak(){
        return streak_;
    }

    public boolean finished() { return (time_ - START_TIME - 1) > songData_.length(); }

    public int stars(){
        if(accuracy() > 0.9f){
            return 3;
        }
        else if(accuracy() > 0.8f){
            return 2;
        }
        else if(accuracy() > 0.6f){
            return 1;
        }
        else return 0;
    }

    public void updateSongData(){
        songData_.updateScore(stars(), correct_, total_);
    }

    public void pauseMusic(){
        isPaused_ = true;
        music_.pause();
    }

    public void resumeMusic(){
        playMusic();
        isPaused_ = false;
    }

    public void playMusic(){
        //make sure music is only played once
        if(!music_.isPlaying() && time_ > START_TIME && (!wasPlayed_ || isPaused_)){
            music_.play();
            wasPlayed_ = true;
        }
    }

    //computes alpha value for active note beat animation
    //it is sort of an exponential function
    public float deltaToActiveValue(float delta){
        float minVal = 0f;
        int steepness = 2;
        int accuracy = 5, accuracyPow2 = 32;

        //normalizing to fit 0..1 scale
        delta = Math.abs(delta/NOTE_TOLERANCE*2);
        if(delta > 1) return 0f;

        //exponentiation
        float result = 1 + -1*(delta*steepness)*(delta*steepness)/accuracyPow2;
        for(int i = 0; i<accuracy; i++) result*=result;

        //accounting for minVal and returning
        return minVal + (1-minVal) * result;


    }

}
