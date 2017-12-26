import java.io.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Scanner;
import javax.imageio.*;
import java.util.zip.Inflater;

public class Img{
	
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		File file;
		String path,fileName;
		///
		do{
			System.out.println("��������Ҫת����ͼƬ·��(-1�˳�)��");
			path = in.nextLine();
			if(path.equals("-1")){
				break;
			}
			file = new File(path);
			if(!file.exists()){
				System.out.println("���ļ�������");
				continue;
			}
			System.out.println("������������ļ�����");
			fileName = in.nextLine();
			byte[] data;
			file = new File(path);
			try{
				//ת������
				BufferedImage image = ImageIO.read(file);
				//��Ϊ�Ҷ�ͼ��
				image = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
					.filter(image, null);
				//��ȡ��������
				data = (byte[])image.getData().getDataElements(
					0,0,image.getWidth(),image.getHeight(),null);
				//���
				output(data,fileName,image.getWidth(),image.getHeight());
			}catch(IOException ioe){
				System.out.println(ioe);
			}
//			output(trans(file), fileName);
		}while(true);
	}
	
	public static void output(byte[] image,String name,int w,int h){
		BufferedWriter bw;
		if(image == null)
			return;
		File file = new File(name);
		try{
//			if(file.exists()){
//				System.out.print("���ļ��Ѵ���,�Ƿ������");
//			}
			file.createNewFile();
			bw = new BufferedWriter( new FileWriter(file));
			char[] text = new char[10000];
			int i,j,x,y,len,cf;
			//��Ȼ��Ҳ��֪��Ϊʲô���ص����鳤����ʱ!=w*h
			cf = image.length/w/h;
			for(i=y=x=0,len=w*h; i<len; i++,x+=cf){
				//[ .'^-"~+*/coxm#$NM%@]
				if((image[x]&0xFF)<=12) 	text[y++]='#'; 
				else if((image[x]&0xFF)<=25) text[y++]='@';
				else if((image[x]&0xFF)<=38) text[y++]='%';
				else if((image[x]&0xFF)<=51) text[y++]='M';
				else if((image[x]&0xFF)<=63) text[y++]='N';
				else if((image[x]&0xFF)<=76) text[y++]='$';
				else if((image[x]&0xFF)<=89) text[y++]='m';
				else if((image[x]&0xFF)<=102) text[y++]='x';
				else if((image[x]&0xFF)<=114) text[y++]='o';
				else if((image[x]&0xFF)<=127) text[y++]='c';
				else if((image[x]&0xFF)<=140) text[y++]='/';
				else if((image[x]&0xFF)<=153) text[y++]='*';
				else if((image[x]&0xFF)<=165) text[y++]='+';
				else if((image[x]&0xFF)<=178) text[y++]='~';
				else if((image[x]&0xFF)<=191) text[y++]='"';
				else if((image[x]&0xFF)<=204) text[y++]='-';
				else if((image[x]&0xFF)<=216) text[y++]='^';
				else if((image[x]&0xFF)<=229) text[y++]='\47';
				else if((image[x]&0xFF)<=242) text[y++]='.';
				else text[y++]=' ';
				if(y == 9990){
					//����
					bw.write(text,0,9990);
					y=0;
				}
				if(i!=0&&i%w==0)
					text[y++]='\n';
			}
			bw.write(text,0,y);
			bw.close();
		}catch(IOException ioe){
			System.out.println(ioe);
		}
	}
	
	public static int[][] trans(File file){
		DataInputStream dis = null;
		int i,j,prog;	//prog���ڼ�¼chunk˳��
		int[][] image = null;
		try{
			dis = new DataInputStream(new FileInputStream(file));
			i = dis.readInt();
			j = dis.readInt();
			if(i != 0x89504E47 || j != 0x0D0A1A0A){
				System.out.println("�ļ���ʽ����");
				dis.close();
				return null;
			}
			///��ʽ��ʼ�����ļ�
			int len,chunk,width = 0,height = 0;	//��¼chunk��С,��chunk����
			int[] paint;	//ɫ��
			byte depth,colorType,rarType,filter,interlace;
			byte[] data,decompressData;
			do{
				len = dis.readInt();
				chunk = dis.readInt();
				switch(chunk){
					case 0x49484452:	//IHDR �ļ�ͷ���ݿ�
						chunk = 0;
						width = dis.readInt();
						height = dis.readInt();
						depth = dis.readByte();
						colorType = dis.readByte();
						rarType = dis.readByte();
						filter = dis.readByte();
						interlace = dis.readByte();
						dis.readInt();	//CRCУ��
						if(rarType!=0){
							System.out.println("����ʶ���ѹ��ģʽ����");
							dis.close();
							return null;
						}
						if(interlace != 0){
							System.out.println("����ɨ��ʽ�洢");
							dis.close();
							return null;
						}
						break;
						
					case 0x504c5445:	//PLTE ��ɫ�����ݿ�
						j = len/3;
						paint = new int[j];
						for(i=0;i<j;i++){
							byte a,b,c;
							a = dis.readByte();
							b = dis.readByte();
							c = dis.readByte();
							paint[i] = (byte)(((a&0xFF)*30 + (b&0xFF)*59 + (c&0xFF)*11 + 50) / 100);
						}
						dis.readInt();	//CRCУ��
						break;
						
					case 0x49444154:	//IDAT ͼ�����ݿ�
						chunk = 1;
						data = new byte[len];
						for(i=0;i<data.length;i++)
							data[i] = dis.readByte();
						decompressData = decompress(data);
						image = getImage(decompressData,width,height);
						dis.readInt();
						return image;
						//break;
						
					case 0x49454e44:	//IEND ͼ���������
						chunk = 2;
						dis.readInt();	//CRC
						break;
						
					case 0x6348524d:	//cHRM ��ɫ�Ͱ�ɫ�����ݿ�
						System.out.println("��ȡ��cHRM");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x71414d41:	//qAMA ͼ������ݿ�
						System.out.println("��ȡ��qAMA");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x73424954:	//sBIT ������Чλ���ݿ�
						System.out.println("��ȡ��sBIT");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x624b4744:	//bKGD ������ɫ���ݿ�
						System.out.println("��ȡ��bkGD");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x68495354:	//hIST 	ͼ��ֱ��ͼ���ݿ�
						System.out.println("��ȡ��hIST");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x70485973:	//pHYs �������سߴ����ݿ�
						System.out.println("��ȡ��pHYs");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74494d45:	//tIME ͼ������޸�ʱ�����ݿ�
						System.out.println("��ȡ��tIME");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74455874:	//tEXt �ı���Ϣ���ݿ�
						System.out.println("��ȡ��tEXt");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x7a545874:	//zTXt ѹ���ı����ݿ�
						System.out.println("��ȡ��zTXt");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x73524742:	//sRGB
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x69434350:	//iCCP
						System.out.println("��ȡ��iCCP");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74524e53:	//tRNS
						System.out.println("��ȡ��tRNS");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x73504c54:	//sPLT
						System.out.println("��ȡ��sPLT");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x69545874:	//iTXt
						System.out.println("��ȡ��iTXt");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
				}
			}while(true);
		}catch(FileNotFoundException fnfe){
			System.out.println("FNFE" + fnfe);
		}catch(EOFException EOF){
			System.out.println("��ȡ����");
		}catch(IOException ioe){
			System.out.println("IOE" + ioe);
		}
		///
		try{
			if(dis!=null)
				dis.close();
		}catch(IOException ioe){
			System.out.println("�ļ��ر�ʧ��" + ioe);
		}
		return image;
	}
	
    /**
     * ��ѹ��
     * @param data ����ѹ������
     * @return byte[] ��ѹ���������
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

	/**
	 * ���ڽ���ѹ����ת��Ϊ����ͼ
	 * data: �ѽ�ѹ����
	 **/
	public static int[][] getImage(byte[] data,int w,int h){
		int[][] image = null;
		int i,j,k,n;
		image = new int[h][w];
//		for(byte bb:data)
//			System.out.print((bb&0xFF)+" ");
//		System.out.println();
		for(i=j=0,k=-1;i<data.length;){
			if(k==-1){ k++;	i++;}
			if((data.length-h)/(w*h)==4){
				i++;//��alpha͸��ɫ
				image[j][k++] = (((data[i++]&0xFF)*30 + (data[i++]&0xFF)*59 + (data[i++]&0xFF)*11 + 50) / 100);
			}else{ //����������RGB����
				image[j][k++] = (((data[i++]&0xFF)*30 + (data[i++]&0xFF)*59 + (data[i++]&0xFF)*11 + 50) / 100);
			}
			if(k == w){ j++;	k = -1;}
		}
		return image;
	}
	
}