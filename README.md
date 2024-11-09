# 介绍

这是一个让服务器与你的爱发电进行自助赞助或者充值系统

## 依赖

- [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/ "PlayerPoints")

## 配置

```yaml
# 开发者站点 https://afdian.com/dashboard/dev
# web服务
web:
  # 端口 默认8000
  port: 8000
# 充值比例 1 = 10
scale: 10
# 爱发电配置
afd:
  # 开发者 token
  token: ""
  # 开发者 userId
  userId: ""
```

## 命令与权限

|    命令    |            权限            | 默认 |
|:--------:|:------------------------:|:--:|
| FAfdOpen | cc.mcyx.fastafdpay.open  | OP |