package com.supertempo.Screens.Game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.supertempo.Screens.Game.GameScreen;
import com.supertempo.Screens.Game.GameWorld;
import com.supertempo.SuperTempo;

/**
 * Created by Dominik on 6/22/2017.
 */

public class GameInputHandler extends InputAdapter {

    private GameWorld gameWorld_;

    public GameInputHandler(GameWorld gameWorld){
        gameWorld_ = gameWorld;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button){

        gameWorld_.touchDown(x, y);

        return true;
    }

    @Override
    public boolean keyDown(int keyCode){
        if(keyCode >= Input.Keys.NUMPAD_1 && keyCode <= Input.Keys.NUMPAD_9){
            int keyId = keyCode - Input.Keys.NUMPAD_1;
            if(keyId < 3) keyId += 6;
            else if (keyId > 5) keyId -=6;
            gameWorld_.song_.hitNote(keyId);
        }

        if(keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK){
            GameScreen gameScreen = SuperTempo.instance.gameScreen;
            if(!gameScreen.isPaused_){
                gameScreen.pause();
            }
            else{
                gameScreen.resume();
            }
        }

        return true;
    }
}
