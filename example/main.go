package main

import (
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"log"
)

type User struct {
	ID    uint    // 主键
	Name  string  // 姓名
	Email *string // 邮箱
}

func (u *User) TableName() string {
	return "users"
}

type User2 struct {
	ID    uint    // 主键2
	Name  string  // 姓名2
	Email *string // 邮箱2
}

type Post struct {
	ID         int32 `gorm:"column:id;type:int(12);primaryKey" json:"id"`      // 主讲
	Context    int32 `gorm:"column:body;type:tinyint(4);not null" json:"body"` // 内容
	CreateTime int32 `gorm:"column:created_at;not null" json:"created_at"`     // 创建时间
}

func main() {
	dsn := ""
	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatal(err)
	}

	// @Model(User)
	query1 := db.Select("id", "name")
	query1 = query1.Where("name = ?", "")

	var user2 User2
	query2 := db.Where("name <> ?", "")
	query2.Where("id > ?").Find(&user2)

	var post Post
	query3 := db.Select("body", "created_at")
	query3 = query3.Where("id IN ?", "")
	query3.Find(&post)
}
