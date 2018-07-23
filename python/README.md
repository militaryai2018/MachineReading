# python版本评测系统

## 使用说明

- 请安装anaconda，清华anaconda下载链接：[清华anaconda软件源链接](https://mirrors.tuna.tsinghua.edu.cn/anaconda/archive/)
- 配置并进入conda环境
  - windows 用户请打开Anaconda Prompt，即可运行conda相关指令
  - Linux或Mac用户请将指令: ". /home/ricky/anaconda3/etc/profile.d/conda.sh"添加到shell配置文件中(如bash，则添加到.bashrc文件中)
- 进入python文件夹，并运行以下指令构建虚拟环境

```bash
conda create -n [virtualenv name] python=3.6 # 请使用自己的环境名替代
conda activate [virtualenv name]
conda install numpy
python test_*.py # 测试用例已经写入test_*.py文件
```