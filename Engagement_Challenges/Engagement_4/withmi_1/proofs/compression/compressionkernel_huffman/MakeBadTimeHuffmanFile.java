import java.io.OutputStream;
import java.io.FileOutputStream;

public class MakeBadTimeHuffmanFile{
    public static void main(String[] args){
        try(OutputStream str = new FileOutputStream("newBadTime.cmpr")) {

            BinaryOut out = new BinaryOut(str);
            int zeroInt = 0;
            out.write(zeroInt); // no padding
            out.writeBit(false); // node not a leaf

            out.writeBit(true); // left leaf
            out.write('A', 16); // 'A' for this node
            out.writeBit(true); // right leaf
            out.write('B', 16); // 'B' for this node


            out.write(Integer.MAX_VALUE);
            for (int i=0; i<13; i++) {// make even number of bytes
                out.writeBit(false);
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


