package com.supertempo.Screens.Game;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.supertempo.Grid;
import com.supertempo.Lane;
import com.supertempo.Resources.SongData;
import com.supertempo.Song;
import com.supertempo.SuperTempo;

import java.util.ArrayList;

/**
 * Created by Dominik on 6/24/2017.
 */

public class GameWorld {

    public Vector2 res_;

    static final int GRID_W = 3, GRID_H = 3;
    Rectangle gridRect_, smallGridRect_;
    static final float
            smallGridScale = 1f,
            smallGridYShift = 0.0f,
            smallGridXShift = 0.0f;

    Grid mainGrid_, smallGrid_;

    public ArrayList<Lane> lanes_;

    public Song song_;

    public GameWorld(Vector2 res, SongData songData, Music music){
        res_ = res;

        gridRect_ = new Rectangle(0, res.y-res.x, res.x, res.x);
        smallGridRect_ = Grid.shrink(gridRect_, smallGridScale);
        smallGridRect_.y -= res.y*smallGridYShift;
        smallGridRect_.x += res.x*smallGridXShift;

        mainGrid_ = new Grid(gridRect_, GRID_W, GRID_H);
        smallGrid_ = new Grid(smallGridRect_, GRID_W, GRID_H);

        lanes_ = new ArrayList<Lane>(GRID_H*GRID_W);
        for(int i = 0; i<GRID_H; i++){
            for(int j = 0; j<GRID_W; j++){
                lanes_.add(new Lane(smallGrid_.midPoint(i, j), mainGrid_.midPoint(i, j), new Vector2(0, 0), mainGrid_.elementSize()));
            }
        }

        song_ = new Song(songData, music);
    }

    public void update(float delta){

        song_.updateTime(delta);

    }

    public void touchDown(int x, int y){
        for(int i = 0; i<lanes_.size(); i++){
            Lane lane = lanes_.get(i);
            if(lane.click(x, y)){
                song_.hitNote(i);
            }
        }
    }

    public static int laneCount(){
        return GRID_W * GRID_H;
    }
}
