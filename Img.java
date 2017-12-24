import java.io.*;
import java.util.Scanner;
import java.util.zip.Inflater;

public class Img{
	
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		File file;
		String path,fileName;
		///
		do{
			System.out.println("请输入需要转换的图片路径(-1退出)：");
			path = in.nextLine();
			if(path.equals("-1")){
				break;
			}
			file = new File(path);
			if(!file.exists()){
				System.out.println("该文件不存在");
				continue;
			}
			System.out.println("请输入输出的文件名：");
			fileName = in.nextLine();
			output(trans(file), fileName);
		}while(true);
	}
	
	public static int[][] trans(File file){
		DataInputStream dis = null;
		int i,j,prog;	//prog用于记录chunk顺序
		int[][] image = null;
		try{
			dis = new DataInputStream(new FileInputStream(file));
			i = dis.readInt();
			j = dis.readInt();
			if(i != 0x89504E47 || j != 0x0D0A1A0A){
				System.out.println("文件格式错误");
				dis.close();
				return null;
			}
			///正式开始解析文件
			int len,chunk,width = 0,height = 0;	//记录chunk大小,及chunk类型
			int[] paint;	//色板
			byte depth,colorType,rarType,filter,interlace;
			byte[] data,decompressData;
			do{
				len = dis.readInt();
				chunk = dis.readInt();
				switch(chunk){
					case 0x49484452:	//IHDR 文件头数据块
						chunk = 0;
						width = dis.readInt();
						height = dis.readInt();
						depth = dis.readByte();
						colorType = dis.readByte();
						rarType = dis.readByte();
						filter = dis.readByte();
						interlace = dis.readByte();
						dis.readInt();	//CRC校验
						if(rarType!=0){
							System.out.println("不能识别的压缩模式代码");
							dis.close();
							return null;
						}
						if(interlace != 0){
							System.out.println("隔行扫描式存储");
							dis.close();
							return null;
						}
						break;
						
					case 0x504c5445:	//PLTE 调色板数据块
						j = len/3;
						paint = new int[j];
						for(i=0;i<j;i++){
							byte a,b,c;
							a = dis.readByte();
							b = dis.readByte();
							c = dis.readByte();
							paint[i] = (byte)(((a&0xFF)*30 + (b&0xFF)*59 + (c&0xFF)*11 + 50) / 100);
						}
						dis.readInt();	//CRC校验
						break;
						
					case 0x49444154:	//IDAT 图像数据块
						chunk = 1;
						data = new byte[len];
						for(i=0;i<data.length;i++)
							data[i] = dis.readByte();
						decompressData = decompress(data);
						image = getImage(decompressData,width,height);
						dis.readInt();
						return image;
						//break;
						
					case 0x49454e44:	//IEND 图像结束数据
						chunk = 2;
						dis.readInt();	//CRC
						break;
						
					case 0x6348524d:	//cHRM 基色和白色点数据块
						System.out.println("读取到cHRM");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x71414d41:	//qAMA 图像γ数据块
						System.out.println("读取到qAMA");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x73424954:	//sBIT 样本有效位数据块
						System.out.println("读取到sBIT");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x624b4744:	//bKGD 背景颜色数据块
						System.out.println("读取到bkGD");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x68495354:	//hIST 	图像直方图数据块
						System.out.println("读取到hIST");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x70485973:	//pHYs 物理像素尺寸数据块
						System.out.println("读取到pHYs");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74494d45:	//tIME 图像最后修改时间数据块
						System.out.println("读取到tIME");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74455874:	//tEXt 文本信息数据块
						System.out.println("读取到tEXt");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x7a545874:	//zTXt 压缩文本数据块
						System.out.println("读取到zTXt");
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
						System.out.println("读取到iCCP");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x74524e53:	//tRNS
						System.out.println("读取到tRNS");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x73504c54:	//sPLT
						System.out.println("读取到sPLT");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
						
					case 0x69545874:	//iTXt
						System.out.println("读取到iTXt");
						for(i=0;i<len;i++)
							dis.readByte();
						dis.readInt();
						break;
				}
			}while(true);
		}catch(FileNotFoundException fnfe){
			System.out.println("FNFE" + fnfe);
		}catch(EOFException EOF){
			System.out.println("读取结束");
		}catch(IOException ioe){
			System.out.println("IOE" + ioe);
		}
		///
		try{
			if(dis!=null)
				dis.close();
		}catch(IOException ioe){
			System.out.println("文件关闭失败" + ioe);
		}
		return image;
	}
	
    /**
     * 解压缩
     * @param data 待解压的数据
     * @return byte[] 解压缩后的数据
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
	 * 用于将解压数据转换为点阵图
	 * data: 已解压数据
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
				i++;//带alpha透明色
				image[j][k++] = (((data[i++]&0xFF)*30 + (data[i++]&0xFF)*59 + (data[i++]&0xFF)*11 + 50) / 100);
			}else{ //不出意外是RGB三连
				image[j][k++] = (((data[i++]&0xFF)*30 + (data[i++]&0xFF)*59 + (data[i++]&0xFF)*11 + 50) / 100);
			}
			if(k == w){ j++;	k = -1;}
		}
		return image;
	}
	
	public static void output(int[][] image,String name){
		BufferedWriter bw;
		if(image == null)
			return;
		File file = new File(name);
		try{
			file.createNewFile();
			bw = new BufferedWriter( new FileWriter(file));
			char[] text = new char[image.length*(image[0].length+1)];
			int i=0;
			for(int x=0;x<image.length;x++){
				for(int y=0;y<image[0].length;y++){
					if(image[x][y]<=26) text[i++]='#'; //分级赋值
					else if(image[x][y]<=51) text[i++]='%';
					else if(image[x][y]<=77) text[i++]='m';
					else if(image[x][y]<=102) text[i++]='x';
					else if(image[x][y]<=128) text[i++]='o';
					else if(image[x][y]<=153) text[i++]='c';
					else if(image[x][y]<=179) text[i++]='+';
					else if(image[x][y]<=204) text[i++]='^';
					else if(image[x][y]<=230) text[i++]='"';
					else text[i++]=' ';
				}
				text[i++]='\n';
			}
			bw.write(text,0,text.length);
			bw.close();
		}catch(IOException ioe){
			System.out.println(ioe);
		}
	}
}
//[ .-+coxm%#@]