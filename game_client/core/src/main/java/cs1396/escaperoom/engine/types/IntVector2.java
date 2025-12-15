package cs1396.escaperoom.engine.types;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class IntVector2 {
    public int x,y;
    public IntVector2(int x, int y){
      this.x = x;
      this.y = y;
    }
    public IntVector2(float x, float y){
      this.x = MathUtils.floor(x);
      this.y = MathUtils.floor(y);
    }
    public IntVector2(){
      this.x = 0;
      this.y = 0;
    }


    @Override
    public String toString() {
      return "(" + this.x + ", " + this.y + ")";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof IntVector2){
        IntVector2 iv2 = (IntVector2)obj;
        return iv2.x == x && iv2.y == y;
      }
      return false;
    }

    public boolean contained(int minX, int minY, int maxX, int maxY){
      return !(x < minX || y < minY || x > maxX || y > maxY);
    }

    public Vector2 asVector2(){
      return new Vector2(x,y);
    }
    public static IntVector2 fromVector2(Vector2 vector){
      return new IntVector2(vector.x, vector.y);
    }

    public IntVector2 cpy(){
      return new IntVector2(x,y);
    }
    public IntVector2 dst(){
      return new IntVector2(x,y);
    }
    public IntVector2 add(IntVector2 other){
      x += other.x;
      y += other.y;
      return this;
    }
    public IntVector2 add(int x, int y){
      this.x += x;
      this.y += y;
      return this;
    }
    public IntVector2 set(int x, int y){
      this.x = x;
      this.y = y;
      return this;
    }
    public IntVector2 set(float x, float y){
      return set((int)x, (int)y);
    }

    public boolean equals(int x, int y){
      return this.x == x && this.y == y;
    }

    public boolean equals(float x, float y){
      return equals((int)x, (int)y);
    }

    public IntVector2 sub(IntVector2 other){
      x -= other.x;
      y -= other.y;
      return this;
    }
    public IntVector2 sub(int x, int y){
      this.x -= x;
      this.y -= y;
      return this;
    }
  }
