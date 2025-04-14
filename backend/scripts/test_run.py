import os
import shutil
import argparse

# 复制视频
def copy_video(input_path, output_path):
    try:
        shutil.copy(input_path, output_path)
        print(f"视频已复制为 {output_path}")
    except Exception as e:
        print(f"复制视频出错: {e}")

# 主函数
def main():
    # 设置命令行参数
    parser = argparse.ArgumentParser(description="Copy the first video to a new directory named after both videos")
    parser.add_argument('--folder1', type=str, required=True, help="Path to the first video file")
    parser.add_argument('--folder2', type=str, required=True, help="Path to the second video file")

    args = parser.parse_args()

    # 获取输入文件路径
    input_path1 = args.folder1
    input_path2 = args.folder2

    # 确保两个视频在同一目录下
    dir1 = os.path.dirname(input_path1)
    dir2 = os.path.dirname(input_path2)
    if dir1 != dir2:
        print("错误：两个视频必须在同一目录下！")
        return

    # 生成目标文件夹路径
    filename1 = os.path.splitext(os.path.basename(input_path1))[0]
    filename2 = os.path.splitext(os.path.basename(input_path2))[0]
    output_dir = os.path.join(dir1, filename1 + filename2)

    # 创建目录（如果不存在）
    os.makedirs(output_dir, exist_ok=True)
    
    # 目标视频路径
    output_path = os.path.join(output_dir, "output.mp4")

    # 复制视频
    copy_video(input_path1, output_path)

if __name__ == "__main__":
    main()