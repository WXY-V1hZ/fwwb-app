from flask import Flask, request, send_file, jsonify
import os
import datetime
import random
import string
import subprocess
import ffmpeg

app = Flask(__name__)

BASE_PATH = os.path.dirname(os.path.abspath(__file__))
IMAGE_FOLDER = os.path.join(BASE_PATH, "assets", "image")
VIDEO_FOLDER = os.path.join(BASE_PATH, "assets", "video")
SCRIPT_PATH = os.path.join(BASE_PATH, "scripts", "test_run.py")

EXAMPLES_FOLDER_NAME = "examples"
OUTPUT_NAME = "output.mp4"
FIRST_TS_NAME = "0000.ts"
M3U8_NAME = "index.m3u8"
TS_NAME = "index.ts"
IMAGE_SUFFIX = ".png"
VIDEO_CODE_TEMP_FILE_SUFFIX = "_temp"
VIDEO_CODE_HEVC = "hevc"

os.makedirs(IMAGE_FOLDER, exist_ok=True)
os.makedirs(VIDEO_FOLDER, exist_ok=True)
os.makedirs(os.path.join(BASE_PATH, "assets", "scripts"), exist_ok=True)

class ResponseCode:
    CODE_200 = {"code": 200, "msg": "请求成功"}
    CODE_404 = {"code": 404, "msg": "请求地址不存在"}
    CODE_600 = {"code": 600, "msg": "请求参数错误"}
    CODE_601 = {"code": 601, "msg": "信息已经存在"}
    CODE_500 = {"code": 500, "msg": "服务器返回错误，请联系管理员"}
    CODE_901 = {"code": 901, "msg": "未登录或登录超时，请重新登录"}

def get_random_string(count):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=count))

def get_file_suffix(filename):
    if not filename or "." not in filename:
        return None
    return filename[filename.rfind("."):]

def path_is_ok(path):
    if not path:
        return True
    if "../" in path or "..\\" in path:
        return False
    return True

def success_response(data):
    return {
        "status": "success",
        "code": ResponseCode.CODE_200["code"],
        "info": ResponseCode.CODE_200["msg"],
        "data": data
    }

def error_response(code, message=None):
    if isinstance(code, dict):
        return {
            "status": "error",
            "code": code["code"],
            "info": message if message else code["msg"]
        }
    return {
        "status": "error",
        "code": code,
        "info": message
    }

def execute_command(cmd):
    app.logger.info(f"Executing command: {cmd}")
    try:
        result = subprocess.run(cmd, shell=True, check=True, capture_output=True, text=True)
        app.logger.info(f"Command result: {result.stdout}")
        return result.stdout
    except subprocess.CalledProcessError as e:
        app.logger.error(f"Command execution failed: {e}")
        raise Exception(f"Command execution failed: {e}")

def create_video_thumbnail(video_path, image_path):
    try:
        image_folder = os.path.dirname(image_path)
        os.makedirs(image_folder, exist_ok=True)

        if os.path.exists(image_path):
            os.remove(image_path)
        
        ffmpeg.input(video_path).output(image_path, vframes=1, q=2).run(quiet=True)
        app.logger.info(f"Created thumbnail: {image_path}")
        return True
    except Exception as e:
        app.logger.error(f"Error creating thumbnail: {e}")
        return False

def get_video_codec(video_path):
    try:
        probe = ffmpeg.probe(video_path)
        video_stream = next((stream for stream in probe['streams'] if stream['codec_type'] == 'video'), None)
        if video_stream:
            return video_stream['codec_name']
        return ""
    except Exception as e:
        app.logger.error(f"Error getting video codec: {e}")
        return ""

def convert_hevc_to_mp4(input_path, output_path):
    try:
        ffmpeg.input(input_path).output(output_path, vcodec='libx264', crf=20).run(quiet=True)
        app.logger.info(f"Converted HEVC to MP4: {output_path}")
        return True
    except Exception as e:
        app.logger.error(f"Error converting HEVC to MP4: {e}")
        return False

def convert_video_to_ts(ts_folder, video_path):
    try:
        m3u8_path = os.path.join(ts_folder, M3U8_NAME)
        segment_pattern = os.path.join(ts_folder, "%04d.ts")
        
        ffmpeg.input(video_path).output(
            segment_pattern,
            c='copy',
            f='segment',
            segment_list=m3u8_path,
            segment_time=10
        ).run(quiet=False)

        if os.path.exists(video_path):
            os.remove(video_path)
            
        app.logger.info(f"Converted video to TS: {video_path}")
        return True
    except Exception as e:
        app.logger.error(f"Error converting video to TS: {e}")
        return False

@app.route('/imageResource')
def get_resource():
    image_path = request.args.get('imagePath')
    
    if not image_path or not path_is_ok(image_path):
        return jsonify(error_response(ResponseCode.CODE_600)), 400
    
    suffix = get_file_suffix(image_path)
    
    file_path = os.path.join(IMAGE_FOLDER, image_path)

    print("imagePath: " + file_path)
    
    if not os.path.exists(file_path):
        return jsonify(error_response(ResponseCode.CODE_404)), 404
    
    content_type = 'image/png'
    if suffix:
        content_type = f'image/{suffix.replace(".", "")}'
    
    return send_file(file_path, mimetype=content_type, max_age=259200)

@app.route('/uploadVideo', methods=['POST'])
def upload_video():
    if 'file' not in request.files:
        return jsonify(error_response(ResponseCode.CODE_600, "No file part")), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify(error_response(ResponseCode.CODE_600, "No selected file")), 400
    
    day = datetime.datetime.now().strftime("%Y%m%d")
    folder = os.path.join(VIDEO_FOLDER, day)
    os.makedirs(folder, exist_ok=True)
    
    original_filename = file.filename
    if not original_filename or '.' not in original_filename:
        return jsonify(error_response(ResponseCode.CODE_600, "Invalid filename")), 400
    
    file_suffix = get_file_suffix(original_filename)
    
    real_filename = f"{get_random_string(20)}{file_suffix}"
    file_path = os.path.join(folder, real_filename)
    
    file.save(file_path)
    
    image_path = os.path.join(
        IMAGE_FOLDER, 
        day, 
        f"{real_filename[:-len(file_suffix)]}{IMAGE_SUFFIX}"
    )
    create_video_thumbnail(file_path, image_path)
    
    return jsonify(success_response(f"{day}/{real_filename}"))

@app.route('/getResult')
def get_result():
    source_name1 = request.args.get('sourceName1')
    source_name2 = request.args.get('sourceName2')
    
    if not source_name1 or not source_name2:
        return jsonify(error_response(ResponseCode.CODE_600, "Missing source names")), 400
    
    video_path1 = os.path.join(VIDEO_FOLDER, source_name1)
    video_path2 = os.path.join(VIDEO_FOLDER, source_name2)
    
    cmd = f"python \"{SCRIPT_PATH}\" --folder1 \"{video_path1}\" --folder2 \"{video_path2}\""
    try:
        execute_command(cmd)
    except Exception as e:
        return jsonify(error_response(ResponseCode.CODE_500, str(e))), 500
    
    date = source_name1.split('/')[0]
    filename1 = os.path.splitext(os.path.basename(source_name1))[0]
    filename2 = os.path.splitext(os.path.basename(source_name2))[0]
    
    output_folder = f"{date}/{filename1}{filename2}"
    output_path = os.path.join(VIDEO_FOLDER, output_folder, OUTPUT_NAME)
    
    if os.path.exists(output_path):
        codec = get_video_codec(output_path)
        if codec == VIDEO_CODE_HEVC:
            temp_file = f"{output_path}{VIDEO_CODE_TEMP_FILE_SUFFIX}"
            os.rename(output_path, temp_file)
            convert_hevc_to_mp4(temp_file, output_path)
            os.remove(temp_file)
        
        output_dir = os.path.dirname(output_path)
        convert_video_to_ts(output_dir, output_path)
        
        first_ts_path = os.path.join(output_dir, FIRST_TS_NAME)
        if os.path.exists(first_ts_path):
            image_path = os.path.join(
                IMAGE_FOLDER, 
                output_folder + IMAGE_SUFFIX
            )
            create_video_thumbnail(first_ts_path, image_path)
    
    return jsonify(success_response(f"{output_folder}/"))

@app.route('/videoResource/<date>/<folderPath>')
def video_resource(date, folderPath):
    file_path = os.path.join(VIDEO_FOLDER, date, folderPath, M3U8_NAME)
    
    if not os.path.exists(file_path):
        return jsonify(error_response(ResponseCode.CODE_404)), 404
    
    return send_file(file_path, mimetype='application/vnd.apple.mpegurl')

@app.route('/videoResource/<date>/<folderPath>/<fileName>')
def video_resource_ts(date, folderPath, fileName):
    file_path = os.path.join(VIDEO_FOLDER, date, folderPath, fileName)
    
    if not os.path.exists(file_path):
        return jsonify(error_response(ResponseCode.CODE_404)), 404
    
    return send_file(file_path, mimetype='video/mp2t')

@app.route('/videoResource/examples/<id>/<folderPath>')
def video_sample_resource(id, folderPath):
    file_path = os.path.join(VIDEO_FOLDER, EXAMPLES_FOLDER_NAME, id, folderPath, M3U8_NAME)
    app.logger.info(f"Serving example index: {file_path}")
    
    if not os.path.exists(file_path):
        return jsonify(error_response(ResponseCode.CODE_404)), 404
    
    return send_file(file_path, mimetype='application/vnd.apple.mpegurl')

@app.route('/videoResource/examples/<id>/<folderPath>/<fileName>')
def video_sample_resource_ts(id, folderPath, fileName):
    file_path = os.path.join(VIDEO_FOLDER, EXAMPLES_FOLDER_NAME, id, folderPath, fileName)
    app.logger.info(f"Serving example TS file: {file_path}")
    
    if not os.path.exists(file_path):
        return jsonify(error_response(ResponseCode.CODE_404)), 404
    
    return send_file(file_path, mimetype='video/mp2t')

@app.errorhandler(404)
def not_found(error):
    return jsonify(error_response(ResponseCode.CODE_404)), 404

@app.errorhandler(Exception)
def handle_exception(e):
    app.logger.error(f"Unhandled exception: {str(e)}")
    return jsonify(error_response(ResponseCode.CODE_500, str(e))), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=7001, debug=True)
