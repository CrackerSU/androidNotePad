# NotePad
This is an AndroidStudio rebuild of google SDK sample NotePad
<img width="1038" height="312" alt="image" src="https://github.com/user-attachments/assets/452a1688-1552-4efc-b32c-0863d02fca4b" />


一、项目概述:

NotePad记事本应用是一款基于Android平台的轻量级笔记管理工具，在原版开源代码基础上进行了功能扩展和界面优化，为用户提供更加完善的笔记记录和管理体验。
本项目基于开源NotePad应用进行功能扩展，在原有基础笔记功能之上，增加了时间戳显示、笔记查询等基本功能，并实现了切换主题、背景颜色和笔记分类两大扩展功能。

二、实验环境
<img width="948" height="273" alt="image" src="https://github.com/user-attachments/assets/25e2c157-cc1b-450a-89e0-b73816d3e535" />

三、新增功能展示

  1.基本功能扩展
  
  1.1 时间戳显示：在笔记列表界面中，每个笔记条目现在都会显示创建或修改的时间戳
  
<img width="347" height="495" alt="image" src="https://github.com/user-attachments/assets/4a7ff07a-97de-4829-acce-154040935306" />
    
  如图所示，每条笔记标题下方都有时间戳显示，记录了每条笔记创建或上次修改时的具体时间，且笔记列表会根据时间先后来排序。

  1.2 笔记查询功能：支持根据标题或内容关键词进行快速搜索定位
    
<img width="453" height="804" alt="940d147f5b26712ce8ef4da1f96b6e60" src="https://github.com/user-attachments/assets/7495b0e4-ea3c-48a3-8b1f-ea8088c1b8f6" />

  如图所示，笔记列表上方有一个搜索栏，点击“放大镜”即可在输入栏输入要搜索的关键字，输入完毕后将会自动展示含有关键字的笔记，可根据笔记标题或内容搜索。
  
<img width="474" height="834" alt="8c1d46e06bd0196c6ab7d64933e7d5d4" src="https://github.com/user-attachments/assets/0497417a-37da-4e34-962b-7d8707c85fad" />

  如图所示，输入“学习”后下方展示了标题含有该关键词的两条笔记。
  
<img width="447" height="765" alt="205daee4e47d102cb89d3dbd85c0790f" src="https://github.com/user-attachments/assets/7fc73d1c-0a6a-4865-9619-cb128fb467b0" />

  如图所示，输入关键词后下方展示了笔记内容含有该关键词的一条笔记。
  
  2.扩展功能实现
  
  2.1 UI美化：对应用界面进行了优化，包括主题设定、背景更换和编辑器优化
  
<img width="474" height="795" alt="6d8f226560c721c6e45160b7a3721ca6" src="https://github.com/user-attachments/assets/44e17c8d-e07d-4a28-b8af-d1e6b130f574" />
  
  如图所示，点击主页右下方设置按钮，可进入该页面，可切换主页主题和编辑页背景颜色。

<img width="396" height="762" alt="5c09b336ba0ece0a1589dfa1e7652111" src="https://github.com/user-attachments/assets/04824bf7-c039-4376-84f6-280e9203ba7f" />

  浅色主题与前面所展示主页面一致，切换至深色主题之后，背景颜色会变暗，与此同时笔记列表的文字颜色会变成白色以免融入背景色。

<img width="420" height="776" alt="f305dac70885cb26056d201784e3642d" src="https://github.com/user-attachments/assets/319d4612-5cb4-4366-9bcb-6bdc9f35ef67" />

  切换编辑页背景颜色之后，编辑页的颜色会切换至所选择的颜色，图中所示为浅蓝色。

  2.2 笔记分类：实现了笔记的分类管理功能，用户可以创建不同的分类文件夹来组织笔记
  
 <img width="456" height="819" alt="096c28dfee3768ed27a82109dc54a354" src="https://github.com/user-attachments/assets/815d1678-ac2e-4164-9ec9-d49340fbf90f" />
<img width="336" height="399" alt="497a0ee4869b67e82e33e8ee917f8d8e" src="https://github.com/user-attachments/assets/8eea3921-2a19-43cb-8f83-519bd1c2acd0" />
  如图所示，主页搜索栏下方会有当前所展示笔记的类别，点击小三角会跳出来所有分类供选择，点击某一分类后即展示当前分类下的所有笔记。

<img width="462" height="810" alt="009a570bb2ad460afed7dd7653d36c21" src="https://github.com/user-attachments/assets/2d4e47b5-966b-4ae5-ae80-0aeec5eddba6" /><img width="339" height="464" alt="9062585a4b528d9e17374e8e1d2152d2" src="https://github.com/user-attachments/assets/94d0517a-06dd-4723-93f2-22d268efeabf" /> 

  如图所示，进入笔记编辑页面，点击分类框小三角可选择当前笔记的分类，也可以添加一个新的分类。
  

四、关键代码讲解

  1.基本功能扩展
  
    1.1 时间戳显示：在笔记列表界面中，每个笔记条目现在都会显示创建或修改的时间戳
    
    1.2 笔记查询功能：支持根据标题或内容关键词进行快速搜索定位

  2.扩展功能实现
  
    2.1 UI美化：对应用界面进行了优化，包括主题设定、背景更换和编辑器优化
    
    2.2 笔记分类：实现了笔记的分类管理功能，用户可以创建不同的分类文件夹来组织笔记
    
五、总结

通过本次实验，我深入掌握了Android应用开发中的数据存储技术、界面优化方法和功能模块设计。特别是在数据库操作和用户交互设计方面积累了宝贵经验。
