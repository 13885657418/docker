#!/bin/bash
# ==========================================
# Kubernetes kubectl 安装脚本 - CentOS/RHEL
# ==========================================

set -e

echo "==========================================  "
echo "安装 kubectl"
echo "=========================================="

# 下载kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# 验证校验和(可选)
curl -LO "https://dl.k8s.io/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check

# 安装kubectl
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# 清理下载文件
rm kubectl kubectl.sha256

# 验证安装
kubectl version --client

echo "=========================================="
echo "kubectl 安装完成!"
echo "=========================================="
