public class Test {	public void upload(){
		try{
			HttpClient client = establishConnection();
			
			if(client == null){
				return;
			}
			
			Vector<File> successFiles = new Vector<File>(10);
			String startDB = "";
			String endDB = "";
			
			//�t�@�C���̃A�b�v���[�h
			setLog("�A�b�v���[�h�J�n");
			for(int i = 0; i < logFiles_.length; i++){
				if(!isRunning_){
					//�A�b�v���[�h�𒆎~���Đؒf
					releaseConnection(client);
					
					break;
				}
				
				if(logFiles_[i].exists()){
					//�t�@�C���`���̃`�F�b�N�ƃR�����g�̎擾
					FileInputStream fis = new FileInputStream(logFiles_[i]);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    
                    String comment = "";
                    String line = null;
                    if((line = br.readLine()) != null){
                    	if(line.startsWith("#LockyStumbler Log")){
    						//LockyStumbler Log�ɂ�2�s�ڂɔ��p100�����̃R�����g������
    						if((line = br.readLine()) != null){
    							if(line.startsWith("#")){
    								comment = line.substring(1);
    								
    								//�R�����g�s�̌��ɂ��锼�p�X�y�[�X������
    								while(comment.endsWith(" ")){
										comment = comment.substring(0, comment.length() - 1);
									}
    							}
    						}
    					}
                    }
                    
                    fis.close();
                    isr.close();
                    br.close();
                    
                    //POST���\�b�h�̍쐬
					PostMethod uploadMethod = new PostMethod("/member/result.html");
					
					uploadMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
					Part[] parts = { new StringPart("from", "logbrowser"), new StringPart("comment", comment), new FilePart("fileName", logFiles_[i], "text/plain", null) };
					uploadMethod.setRequestEntity(new MultipartRequestEntity(parts, uploadMethod.getParams()));
					
					
					client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
					
					//POST�f�[�^�̑��M
					int statusCode = client.executeMethod(uploadMethod);
					
					if(statusCode == HttpStatus.SC_OK){
    					//�f�[�^�x�[�X�ɓo�^���ꂽ�n�_�ƏI�_���擾����
    					String response = uploadMethod.getResponseBodyAsString();
    					
    					String start = response.substring(0, response.indexOf("\t"));
    					String end = response.substring(response.indexOf("\t") + 1);
    					
    					//�n�_�̏����l�ݒ�
    					if(startDB.equals("")){
    						startDB = start;
    					}
    					
    					//�I�_�̏����l�ݒ�
    					if(endDB.equals("")){
    						endDB = end;
    					}
    					
    					//�I�_�̍X�V
    					if(Integer.parseInt(endDB) < Integer.parseInt(end)){
    						endDB = end;
    					}
    					
    					//���M�����t�@�C���ɒǉ�
    					successFiles.add(logFiles_[i]);
    					
    					//�I���������J�n����Ă���ꍇ�͏o�͂��Ȃ�
    					if(isRunning_){
    						setLog(logFiles_[i].getName() + "\t[ SUCCESS ]");
    					}
					}
					
					uploadMethod.releaseConnection();
					
					setProgress(i + 1);
				}
			}
			if(isRunning_){
				setLog("�A�b�v���[�h�I��");
			}
			
			
			
			//�A�b�v���[�h�̌��ʂ�\��
			String view = readParameter(UPLOAD_RESULT);
			if(!isRunning_){
				//�I���������͌��ʂ�\�����Ȃ�
			}
			else if(view.equals("MAP")){
				//�V�K�����A�N�Z�X�|�C���g���}�b�v�ɕ\������
				MessageDigest md5 = MessageDigest.getInstance("MD5");
            	md5.update(accountName_.getBytes());
            	byte[] digest = md5.digest();
            	
            	//�_�C�W�F�X�g�𕶎���ɕϊ�
            	String userNameDigest = "";
            	for(int i = 0; i < digest.length; i++){
            		int d = digest[i];
            		if(d < 0){
            			//byte�^�ł�128~255�����ɂȂ��Ă���̂ŕ␳
            			d += 256;
            		}
            		if(d < 16){
            			//2���ɒ���
            			userNameDigest += "0";
            		}
            		
            		//�_�C�W�F�X�g�l��1�o�C�g��16�i��2���ŕ\��
            		userNameDigest += Integer.toString(d, 16);
            	}
            	
            	//�n�_�ƏI�_�𐳏�Ɏ擾�ł��Ȃ������ꍇ
            	if(startDB.equals("")){
            		startDB = "0";
            	}
            	if(endDB.equals("")){
            		endDB = "0";
            	}
            	
            	//�V�K����������̏ꍇ�͕\�����Ȃ�
            	if(startDB.equals("0")&&endDB.equals("0")){
            		setLog("�V�K�������F 0");
            	}
            	else{
            		ProcessBuilder process = new ProcessBuilder(readParameter(WEB_BROWSER), "http://" + readParameter(WEB_HOST) + "/service/logviewer.html?user=" + userNameDigest + "&start=" + startDB + "&end=" + endDB);
                	process.start();
            	}
			}
			else if(view.equals("TEXT")){
				if(startDB.equals("")||endDB.equals("")){
            		//���ɕs������ꍇ�͕\�����Ȃ�
					setLog("��M��񂪌����Ă��邽�ߕ\���ł��܂���");
            	}
				else{
					int newCount = Integer.parseInt(endDB) - Integer.parseInt(startDB);
    				setLog("�V�K�������F " + String.valueOf(newCount));
				}
			}
			
			
			//�A�b�v���[�h�����t�@�C���̃t���O��ύX
			for(int i = 0; i < successFiles.size(); i++){
				try{
					RandomAccessFile file = new RandomAccessFile(successFiles.get(i), "rw");
					
					//���O�t�@�C�������擾
					String line;
					String seekString = "";
					while((line = file.readLine()) != null){
						if(line.startsWith("#LockyStumbler Log")){
							
							//���O�t�@�C���̃o�[�W�������m�F
							int version = Integer.parseInt(line.substring("#LockyStumbler Log Version ".length()));
							if(version < 2){
								return;
							}
							
							
							//2�s�ڂ܂ł̕�������L�^
							//seekString += line + "\r\n" + file.readLine() + "\r\n";
							file.readLine();
							long pos = file.getFilePointer();
							
							//3�s�ڂ̕t�������擾
							line = file.readLine();
							String[] token = line.substring(1).split("[|]");
							for(int j = 0; j < token.length; j++){
								if(token[j].startsWith("UPLOAD=")){
									//�t�@�C���̃A�b�v���[�h�t���O���X�V
									//file.seek((seekString + "|UPLOAD=").length());
									file.seek(pos + "|UPLOAD=".length());
									file.write("T".getBytes());
								}
								else{
									//seekString += "|" + token[j];
									pos += ("|" + token[j]).length();
								}
							}
						}
					}
					
					file.close();
				}
				catch(FileNotFoundException exception){
					exception.printStackTrace();
				}
				catch(IOException exception){
					exception.printStackTrace();
				}
			}
			
			//�A�b�v���[�h���f
			if(!isRunning_){
				//�I�������ҋ@���[�v�̉���
				isRunning_ = true;
				return;
			}
			
			//�A�b�v���[�h����I��
			isRunning_ = false;
			enableClose();
			releaseConnection(client);
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
		catch(NoSuchAlgorithmException exception){
			exception.printStackTrace();
			setLog("JRE�̃o�[�W�������Â����ߕ\���ł��܂���ł���");
		}
	}
}