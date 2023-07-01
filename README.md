<!-- Plugin description -->

<div align="center">
    <img src="./src/main/resources/icons/icon64x64.svg" alt="Go ORM Helper"/>
    <h1 align="center">Go ORM Helper</h1>
</div>

<p align="center">一个为了让你在 Goland 上写 ORM 能自动补全数据库字段、Tag、生成Struct 的 Jetbrains 插件。支持：gorm、xorm、beego、database/sql...）</p>

> 灵感来源 [Laravel Idea](https://plugins.jetbrains.com/plugin/13441-laravel-idea) & 
> [PhpStorm metadata](https://www.jetbrains.com/help/phpstorm/ide-advanced-metadata.html)。 本人使用 golang 的 orm 包时，
> 一些 ORM 函数的参数是字符串，并且ide不支持代码补全，在字段贼多的情况下，记不清楚，写起来就会很不方便。以前本人写php的时候，就用到前面的插件，
> 感觉非常爽，所以就有了这个插件~~

## 特性

- [x] ORM 代码补全
  - [x] @Model 注解辅助补全
  - [ ] @Table 注解辅助补全 🚧[WIP]
  - [ ] 自定义 SQL 辅助不全 🚧[WIP]
- [x] SQL 转 Struct
  - [x] Gorm
  - [x] Xorm
  - [x] 通用 Struct
  - [ ] GoFrame
  - [ ] Beego
  - [ ] sqlx
- [ ] Tag 代码补全 🚧[WIP]
- 更多等你去发现与改进...

## 支持的ORM

- [x] [Gorm](https://github.com/go-gorm/gorm)
- [x] [Xorm](https://gitea.com/xorm/xorm)
- [ ] [GoFrame](https://github.com/gogf/gf) 🚧[WIP]
- [ ] [Beego](https://github.com/beego/beego) 🚧[WIP]
- [ ] [sqlx](https://github.com/jmoiron/sqlx) 🚧[WIP]
- 更多等你去发现与改进...

## 使用

![guide.gif](assets%2Fguide.gif)

<!-- Plugin description end -->
