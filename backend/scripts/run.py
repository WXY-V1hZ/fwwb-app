'''
运行脚本命令：
python run.py --folder "xx/xxx/x/xx/xx/xx.png"
'''

import os
import torch
import torch.nn as nn
from PIL import Image
import torchvision.transforms as transforms
import argparse


# 定义 U-Net 网络
import torch
import torch.nn as nn


class UNet(nn.Module):
    def __init__(self, in_channels=3, out_channels=3):
        """
        初始化 U-Net 网络。

        参数：
            in_channels (int): 输入通道数，默认为 1（单通道）。
            out_channels (int): 输出通道数，默认为 1（单通道）。
        """
        super(UNet, self).__init__()
        # 编码器（下采样路径）
        self.enc1 = self.conv_block(in_channels, 64)  # 64
        self.enc2 = self.conv_block(64, 112)  # 112
        self.enc3 = self.conv_block(112, 128)  # 128
        self.enc4 = self.conv_block(128, 256)  # 256

        # 池化层
        self.pool = nn.MaxPool2d(kernel_size=2, stride=2)

        # 解码器（上采样路径）
        self.up3 = nn.ConvTranspose2d(256, 128, kernel_size=2, stride=2)
        self.dec3 = self.conv_block(256, 128)  # 输入通道为 256（128+128）

        self.up2 = nn.ConvTranspose2d(128, 112, kernel_size=2, stride=2)
        self.dec2 = self.conv_block(224, 112)  # 输入通道为 224（112+112）

        self.up1 = nn.ConvTranspose2d(112, 64, kernel_size=2, stride=2)
        self.dec1 = self.conv_block(128, 64)  # 输入通道为 128（64+64）

        # 输出层
        self.out_conv = nn.Conv2d(64, out_channels, kernel_size=1)

    def conv_block(self, in_channels, out_channels):
        """
        定义卷积块：两个 3x3 卷积 + ReLU。

        参数：
            in_channels (int): 输入通道数。
            out_channels (int): 输出通道数。

        返回：
            nn.Sequential: 卷积块。
        """
        return nn.Sequential(
            nn.Conv2d(in_channels, out_channels, kernel_size=3, padding=1),
            nn.ReLU(inplace=True),
            nn.Conv2d(out_channels, out_channels, kernel_size=3, padding=1),
            nn.ReLU(inplace=True)
        )

    def forward(self, x):
        """
        前向传播。

        参数：
            x (torch.Tensor): 输入张量，形状为 (B, 1, H, W)。

        返回：
            torch.Tensor: 输出张量，形状为 (B, 1, H, W)。
        """
        # 编码器
        e1 = self.enc1(x)  # (B, 16, H, W)
        p1 = self.pool(e1)  # (B, 16, H/2, W/2)
        e2 = self.enc2(p1)  # (B, 32, H/2, W/2)
        p2 = self.pool(e2)  # (B, 32, H/4, W/4)
        e3 = self.enc3(p2)  # (B, 64, H/4, W/4)
        p3 = self.pool(e3)  # (B, 64, H/8, W/8)
        e4 = self.enc4(p3)  # (B, 128, H/8, W/8)

        # 解码器
        u3 = self.up3(e4)  # (B, 64, H/4, W/4)
        cat3 = torch.cat([u3, e3], dim=1)  # (B, 128, H/4, W/4)
        d3 = self.dec3(cat3)  # (B, 64, H/4, W/4)

        u2 = self.up2(d3)  # (B, 32, H/2, W/2)
        cat2 = torch.cat([u2, e2], dim=1)  # (B, 64, H/2, W/2)
        d2 = self.dec2(cat2)  # (B, 32, H/2, W/2)

        u1 = self.up1(d2)  # (B, 16, H, W)
        cat1 = torch.cat([u1, e1], dim=1)  # (B, 32, H, W)
        d1 = self.dec1(cat1)  # (B, 16, H, W)

        # 输出
        out = self.out_conv(d1)  # (B, 1, H, W)
        return out


# 数据预处理函数
def load_image(image_path):
    img = Image.open(image_path).convert('RGB')
    transform = transforms.Compose([
        transforms.ToTensor(),
    ])
    img_tensor = transform(img).unsqueeze(0)
    return img_tensor


# 张量转图片并保存
def save_tensor_to_image(tensor, output_path):
    tensor = tensor.squeeze(0)
    img = transforms.ToPILImage()(tensor)
    img.save(output_path)


# 主测试流程
def main():
    # 设置命令行参数
    parser = argparse.ArgumentParser(description="Run U-Net on an image from a specified folder.")
    parser.add_argument('--folder', type=str, required=True, help="Path to the folder containing xxx.png")
    args = parser.parse_args()

    # 获取文件夹路径
    input_path =  args.folder
    output_path = input_path.replace(".png", "_unet.png")

    # 检查输入图片是否存在
    if not os.path.exists(input_path):
        print(f"错误：图片 {input_path} 不存在！")
        return

    # 设备设置
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # 初始化模型
    model = UNet(in_channels=3, out_channels=3).to(device)
    model.eval()

    # 读取图片并转换为张量
    input_tensor = load_image(input_path).to(device)

    # 前向传播
    with torch.no_grad():
        output_tensor = model(input_tensor)

    # 保存输出图片
    save_tensor_to_image(output_tensor.cpu(), output_path)

if __name__ == "__main__":
    main()