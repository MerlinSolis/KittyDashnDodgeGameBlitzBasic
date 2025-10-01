;##################GeneralGameWindowSetupStart############################################################################
AppTitle "Kitty Dash'n Dodge"
Const SCREEN_WIDTH = 1100 : Const SCREEN_HEIGHT = 600
Graphics SCREEN_WIDTH,SCREEN_HEIGHT,0,2
Const FPS_CAP = 30
fps_timer = CreateTimer(FPS_CAP)
SetBuffer BackBuffer()
SeedRnd MilliSecs()
TFormFilter False
Const ASSET_PATH$ = "Assets/"
Global game_speed = 15
Global bg_x_pos = 0
Global game_state = 0 ;1 = game_play, 0 = game_over/start_menu, 2 = win screen
title_font = LoadFont(ASSET_PATH$+"Fonts/modern goth.ttf",150)
normal_font = LoadFont(ASSET_PATH$+"Fonts/Public Pixel.ttf",20)
Global ko_count = 0
Global win_count = 0
Global score = 0
Global hi_score = 0
Global kitties_pacified = 0
Global save_score_dir$ = CurrentDir$()+"Assets/score_data/"
file_in = OpenFile(save_score_dir$+"hi_score.txt")
If file_in = 0 Then
	file_out = WriteFile(save_score_dir$+"hi_score.txt")
	WriteInt file_out,hi_score
	CloseFile file_out
	CloseFile file_in
Else
	hi_score = ReadInt(file_in)
	CloseFile file_in
EndIf



bg_floor_image = LoadImage(ASSET_PATH$+"BG_images/scrolling_floor.png")
game_over_screen_image = LoadImage(ASSET_PATH$+"BG_images/kitty_dodge_game_over.png")
title_screen_image = LoadImage(ASSET_PATH$+"BG_images/Kitty_Dodge_Title2.png")
win_screen_image = LoadImage(ASSET_PATH$+"BG_images/Win_Screen_1.png")
MaskImage win_screen_image,106,190,48
ResizeImage win_screen_image,SCREEN_WIDTH,SCREEN_HEIGHT
MaskImage game_over_screen_image,255,121,48
MaskImage title_screen_image,255,121,48
ScaleImage game_over_screen_image,4.0,4.0
ScaleImage title_screen_image,4.0,4.0
MidHandle game_over_screen_image
MidHandle title_screen_image
bg_wall_image = Create_Kitchen_Wall_IMG(200,200,200)
bg_topwall_image = Create_Kitchen_Wall_IMG(81,38,22)


;################GeneralGameWindowSetupEnd#################################################################################


;#########################DecoObjectDataAndMethods#########################################################################
Dim deco_mask_color(2)
deco_mask_color(0) = 243 : deco_mask_color(1) = 97 : deco_mask_color(2) = 255
Dim deco_image_list(2)
img_scale_factor# = 2.0
deco_image_list(0) = LoadImage(ASSET_PATH$+"BG_images/cooking_pot_stove_01.png")
MaskImage deco_image_list(0),deco_mask_color(0),deco_mask_color(1),deco_mask_color(2)
deco_image_list(1) = LoadImage(ASSET_PATH$+"BG_images/open_fridge_01.png")
MaskImage deco_image_list(1),deco_mask_color(0),deco_mask_color(1),deco_mask_color(2)
deco_image_list(2) = LoadImage(ASSET_PATH$+"BG_images/plate_shelf_01.png")
MaskImage deco_image_list(2),deco_mask_color(0),deco_mask_color(1),deco_mask_color(2)
For img_num = 0 To 2
	ScaleImage deco_image_list(img_num),img_scale_factor#,img_scale_factor#
Next
Global deco_counter = 0
Global deco_last_time_spawned = MilliSecs()
Global deco_spawn_interval = 800

Type Kitchen_Object
	Field x,y
	Field image
	Field image_width
End Type
Function Init_Kitchen_Obj_Self.Kitchen_Object(deco_type$)
	kitchen_deco.Kitchen_Object = New Kitchen_Object
	
	y_offset = Rand(-50,50)
	kitchen_deco\y = SCREEN_HEIGHT / 2 + y_offset
	Select deco_type$
		Case "cooking_pot"
			kitchen_deco\image = deco_image_list(0)
		Case "open_fridge"
			kitchen_deco\image = deco_image_list(1)
		Case "plate_shelf"
			kitchen_deco\image = deco_image_list(2)
	End Select
	kitchen_deco\image_width = ImageWidth(kitchen_deco\image)
	kitchen_deco\x = SCREEN_WIDTH + Rand(800,1000) + kitchen_deco\image_width
	deco_counter = deco_counter + 1
End Function
Function Kitchen_Obj_Update(kitchen_deco_obj.Kitchen_Object)
	kitchen_deco_obj\x = kitchen_deco_obj\x - game_speed / 4
	If kitchen_deco_obj\x <= -kitchen_deco_obj\image_width Then
		Delete kitchen_deco_obj
		deco_counter = deco_counter - 1
	EndIf
End Function
Function Kitchen_Obj_Draw(kitchen_deco_obj.Kitchen_Object)
	If kitchen_deco_obj <> Null Then
		DrawImage kitchen_deco_obj\image,kitchen_deco_obj\x,kitchen_deco_obj\y
	EndIf
End Function 
Function Kitchen_Obj_Spawner()
	If (deco_counter >= 0 And deco_counter <= 6) And (MilliSecs() > (deco_last_time_spawned + deco_spawn_interval)) Then
		random_choice = Rand(0,2)
		Select random_choice
			Case 0
				Init_Kitchen_Obj_Self("cooking_pot")
			Case 1
				Init_Kitchen_Obj_Self("open_fridge")
			Case 2
				Init_Kitchen_Obj_Self("plate_shelf")
		End Select
		deco_last_time_spawned = MilliSecs()
	EndIf
End Function
;###########################DecoObjectDataAndMethodsEnd###################################################################

;##########################PlayerDataAndMethodsStart#####################################################################
Const PLAYER_X_POS = 80
Const PLAYER_Y_POS = 430
Const PLAYER_SLIDE_Y_POS = PLAYER_Y_POS + 34
Const PLAYER_JUMP_VEL# = 8.5
Dim player_mask_color(2)
player_mask_color(0) = 255 : player_mask_color(1) = 162 : player_mask_color(2) = 0
Const K_W = 17
Const K_S = 31
Const K_UP = 200
Const K_DOWN = 208
Const K_SPACE = 57

player_hurt_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_hurt_01.png")
MaskImage player_hurt_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
player_jump_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_jump_01.png")
MaskImage player_jump_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
player_health_heart_img = LoadImage(ASSET_PATH$+"Gui_Elements/health_heart_01.png")
health_heart_pickup_img = CopyImage(player_health_heart_img)
Dim health_heart_mask_col(2)
health_heart_mask_col(0) = 0 : health_heart_mask_col(1) = 0 : health_heart_mask_col(2) = 0
MaskImage player_health_heart_img,health_heart_mask_col(0),health_heart_mask_col(1),health_heart_mask_col(2)
ScaleImage player_health_heart_img,1.5,1.5
Dim player_run_frames(1)
Dim player_slide_frames(1)
For img_num = 0 To 1
	run_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_run_frame_0"+Str((img_num+1))+".png")
	MaskImage run_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
	player_run_frames(img_num) = run_img
	slide_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_slide_frame_0"+Str((img_num+1))+".png")
	MaskImage slide_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
	player_slide_frames(img_num) = slide_img
Next
Type Player_Girl
	Field x,y
	Field image
	Field jump_image
	Field hurt_image
	Field step_index
	Field bool_run,bool_jump,bool_slide
	Field init_x_pos,init_y_pos
	Field slide_y_pos
	Field jumping_vel#
	Field bool_draw_flag
	Field health_points
	Field last_time_hit
	Field hit_flicker_duration
	Field hit_flicker_sine#
	Field bool_hit_flag
	Field health_heart_image
	Field snack_bullet_count
End Type
Function Init_Player_Self.Player_Girl(player_hurt_img,player_jump_img,player_health_heart_img)
	player_girl.Player_Girl = New Player_Girl
	player_girl\init_x_pos = PLAYER_X_POS
	player_girl\init_y_pos = PLAYER_Y_POS
	player_girl\slide_y_pos = PLAYER_SLIDE_Y_POS
	player_girl\x = player_girl\init_x_pos
	player_girl\y = player_girl\init_y_pos
	player_girl\step_index = 0
	player_girl\image = player_run_frames(player_girl\step_index)
	player_girl\hurt_image = player_hurt_img
	player_girl\jump_image = player_jump_img
	player_girl\bool_run = True
	player_girl\bool_jump = False
	player_girl\bool_slide = False
	player_girl\jumping_vel# = PLAYER_JUMP_VEL#
	player_girl\bool_draw_flag = True
	player_girl\health_points = 3
	player_girl\hit_flicker_duration = 1000
	player_girl\hit_flicker_sine# = 0
	player_girl\bool_hit_flag = False
	player_girl\snack_bullet_count = 3
	player_girl\health_heart_image = player_health_heart_img
	Return player_girl.Player_Girl
End Function
Function Player_Girl_Take_Input(player_instance.Player_Girl)
	If (KeyDown(K_DOWN) Or KeyDown(K_S)) And (Not player_instance\bool_slide) Then
		player_instance\bool_slide = True
		player_instance\bool_run = False
		player_instance\bool_jump = False
	ElseIf (KeyHit(K_UP) Or KeyHit(K_W)) And (Not player_instance\bool_jump) Then
		player_instance\bool_slide = False
		player_instance\bool_run = False
		player_instance\bool_jump = True
	ElseIf Not(player_instance\bool_jump Or (KeyDown(K_DOWN) = True Or KeyDown(K_S) = True)) Then
		player_instance\bool_slide = False
		player_instance\bool_run = True
		player_instance\bool_jump = False
	EndIf
	If KeyHit(K_SPACE) And player_instance\snack_bullet_count > 0 Then
		Player_Girl_Shoot_Snacks(player_instance)
	EndIf 
End Function
Function Player_Girl_Shoot_Snacks(player_instance.Player_Girl)
	snack_bullet_object.Snack_Bullet = Init_Snack_Bullet(player_instance\x + ImageWidth(player_instance\image)/2,player_instance\y + ImageHeight(player_instance\image)/2,10)
	player_instance\snack_bullet_count = player_instance\snack_bullet_count - 1
End Function
Function Player_Girl_Run(player_instance.Player_Girl)
	player_instance\image = player_run_frames(Floor(player_instance\step_index / 5))
	player_instance\y = player_instance\init_y_pos
End Function
Function Player_Girl_Jump(player_instance.Player_Girl)
	player_instance\image = player_instance\jump_image
	If player_instance\bool_jump Then
		player_instance\y = player_instance\y - (player_instance\jumping_vel# * 4)
		player_instance\jumping_vel# = player_instance\jumping_vel# - 0.8
		If player_instance\jumping_vel# <= -PLAYER_JUMP_VEL# Then
			player_instance\jumping_vel# = PLAYER_JUMP_VEL#
			player_instance\bool_jump = False
		EndIf
	EndIf
End Function
Function Player_Girl_Slide(player_instance.Player_Girl)
	player_instance\image = player_slide_frames(Floor(player_instance\step_index / 5))
	player_instance\y = player_instance\slide_y_pos
End Function
Function Player_Girl_Update(player_instance.Player_Girl)
	player_instance\step_index = player_instance\step_index + 1
	If player_instance\step_index >= 10 Then
		player_instance\step_index = 0
	EndIf
	
	Player_Girl_Take_Input(player_instance)
	
	If player_instance\bool_run Then
		Player_Girl_Run(player_instance)
	ElseIf player_instance\bool_jump Then
		Player_Girl_Jump(player_instance)
	ElseIf player_instance\bool_slide Then
		Player_Girl_Slide(player_instance)
	EndIf
	
	If player_instance\bool_hit_flag Then
		player_instance\image = player_instance\hurt_image
		Player_Girl_Hit_Flicker(player_instance)
		If MilliSecs() > (player_instance\last_time_hit + player_instance\hit_flicker_duration) Then
			player_instance\bool_hit_flag = False
			player_instance\bool_draw_flag = True
		EndIf
	EndIf 
	
	Player_Girl_Draw_GUI_BG()
	Player_Girl_Show_Health_Hearts(player_instance)
	Player_Girl_Gameover_Check(player_instance)
	
End Function
Function Player_Girl_Draw(player_instance.Player_Girl)
	If player_instance\bool_draw_flag Then
		DrawImage player_instance\image,player_instance\x,player_instance\y
	EndIf
End Function
Function Player_Girl_Hit_Flicker(player_instance.Player_Girl)
	player_instance\hit_flicker_sine# = player_instance\hit_flicker_sine# + 60
	If player_instance\hit_flicker_sine# >= 360 Then
		player_instance\hit_flicker_sine# = 0
	EndIf
	If Sin(player_instance\hit_flicker_sine#) > 0 Then
		player_instance\bool_draw_flag = True
	ElseIf Sin(player_instance\hit_flicker_sine) < 0 Then
		player_instance\bool_draw_flag = False
	EndIf
End Function
Function Player_Girl_Draw_GUI_BG()
	Color 0,0,0
	Rect 0,0,SCREEN_WIDTH,75,True
End Function
Function Player_Girl_Show_Health_Hearts(player_instance.Player_Girl)
	heart_img_width = ImageWidth(player_instance\health_heart_image)
	For heart_number = 0 To player_instance\health_points-1
		DrawImage player_instance\health_heart_image,20 + (heart_number*heart_img_width) + (heart_number*10),10
	Next
End Function
Function Player_Girl_Gameover_Check(player_instance.Player_Girl)
	If player_instance\health_points <= 0 Then
		ko_count = ko_count + 1
		game_state = 0
		Delay 1000
		Cls
		Flip
	EndIf
End Function

player_girl.Player_Girl = Init_Player_Self(player_hurt_img,player_jump_img,player_health_heart_img)
;###################PlayerDataAndMethodsEnd####################################################################


;###################SnackBulletObjectDataAndMethodsStart###########################################################


Dim snack_bullet_image_frames(5)
snack_bullet_image = LoadImage(ASSET_PATH$+"Snack_Bullet_Images/kitty_snack_sprite.png")
MidHandle snack_bullet_image
MaskImage snack_bullet_image,215,123,186
ScaleImage snack_bullet_image,0.5,0.5
For frame_num = 0 To 5
	temp_img = CopyImage(snack_bullet_image)
	angle# = 360 - (frame_num*60)
	RotateImage temp_img,angle#
	snack_bullet_image_frames(frame_num) = temp_img
Next

Type Snack_Bullet
	Field image
	Field step_index
	Field x,y
	Field speed
End Type
Function Init_Snack_Bullet.Snack_Bullet(x,y,speed)
	
	snack_bullet.Snack_Bullet = New Snack_Bullet
	snack_bullet\step_index = 0
	snack_bullet\image = snack_bullet_image_frames(snack_bullet\step_index)
	snack_bullet\x = x
	snack_bullet\y = y
	snack_bullet\speed = speed
	
	Return snack_bullet.Snack_Bullet
End Function
Function Update_Snack_Bullet(snack_bullet_object.Snack_Bullet)
	snack_bullet_object\step_index = snack_bullet_object\step_index + 1
	If snack_bullet_object\step_index >= 18 Then
		snack_bullet_object\step_index = 0
	EndIf
	snack_bullet_object\image = snack_bullet_image_frames(Floor(snack_bullet_object\step_index/3))
	
	snack_bullet_object\x = snack_bullet_object\x + snack_bullet_object\speed
	
	If snack_bullet_object\x > SCREEN_WIDTH Then
		Delete snack_bullet_object
	EndIf
End Function
Function Draw_Snack_Bullet(snack_bullet_object.Snack_Bullet)
	
	If snack_bullet_object <> Null Then
		DrawImage snack_bullet_object\image,snack_bullet_object\x,snack_bullet_object\y
	EndIf
End Function




;###################SnackBulletObjectDataAndMethodsEnd###############################################################


;###################ObstacleObjectDataAndMethodsStart##########################################################

Dim kitty_color_mask(2)
kitty_color_mask(0) = 243 : kitty_color_mask(1) = 97 : kitty_color_mask(2) = 255
Dim sleeping_kitty_frames(17)
For img_num = 0 To 17
	scale_factor# = 0.5
	temp_img = LoadImage(ASSET_PATH$+"Obstacle_Images/sleeping_kitty_frame_"+Str((img_num+1))+".png")
	ScaleImage temp_img,scale_factor#,scale_factor#
	sleeping_kitty_frames(img_num) = temp_img
Next
Dim singing_kitty_frames(2)
For img_num = 0 To 2
	scale_factor# = 0.5
	temp_img = LoadImage(ASSET_PATH$+"Obstacle_Images/singing_scratch_cat_frame_"+Str((img_num+1))+".png")
	MaskImage temp_img,kitty_color_mask(0),kitty_color_mask(1),kitty_color_mask(2)
	ScaleImage temp_img,scale_factor#,scale_factor#
	singing_kitty_frames(img_num) = temp_img
Next

Global flying_obstacle_kitty_img = LoadImage(ASSET_PATH$+"Obstacle_Images/flying_scratch_kitty_left.png")
MaskImage flying_obstacle_kitty_img,kitty_color_mask(0),kitty_color_mask(1),kitty_color_mask(2)
ScaleImage flying_obstacle_kitty_img,0.25,0.25

Global kitty_last_time_spawned = MilliSecs()
Global kitty_spawn_interval = Rand(500,1500)
Global kitty_object_count = 0
Type Obstacle_Kitty
	Field x#,y#
	Field image
	Field image_width
	Field kitty_type$
	Field step_index
End Type
Function Init_Obstacle_Kitty_Self.Obstacle_Kitty(kitty_type$)
	obstacle_kitty.Obstacle_Kitty = New Obstacle_Kitty
	obstacle_kitty\kitty_type$ = kitty_type$
	obstacle_kitty\step_index = 0
	
	Select obstacle_kitty\kitty_type$
		Case "sleeping_kitty"
			obstacle_kitty\image = sleeping_kitty_frames(obstacle_kitty\step_index)
			obstacle_kitty\image_width = ImageWidth(obstacle_kitty\image)
			obstacle_kitty\y = 400 + Rand(-10,10)
		Case "singing_kitty"
			obstacle_kitty\image = singing_kitty_frames(obstacle_kitty\step_index)
			obstacle_kitty\image_width = ImageWidth(obstacle_kitty\image)
			obstacle_kitty\y = 400 + Rand(-10,10)
		Case "flying_kitty"
			obstacle_kitty\image = flying_obstacle_kitty_img
			obstacle_kitty\image_width = ImageWidth(obstacle_kitty\image)
			obstacle_kitty\y = 350 + Rand(-20,20)
	End Select
	obstacle_kitty\x = SCREEN_WIDTH + Rand(800,1000) + obstacle_kitty\image_width
	
	kitty_object_count = kitty_object_count + 1
End Function
Function Obstacle_Kitty_Spawner()
	If (kitty_object_count >= 0 And kitty_object_count <= 2) Then
		If MilliSecs() > (kitty_last_time_spawned + kitty_spawn_interval) Then
			random_choice = Rand(0,3)
			Select random_choice
				Case 0
					Init_Obstacle_Kitty_Self("sleeping_kitty")
				Case 1
					Init_Obstacle_Kitty_Self("singing_kitty")
				Case 2
					Init_Obstacle_Kitty_Self("flying_kitty")
			End Select
			kitty_last_time_spawned = MilliSecs()
			kitty_spawn_interval = Rand(1000,1500)
		EndIf
	EndIf
End Function
Function Obstacle_Kitty_Update(obstacle_kitty.Obstacle_Kitty,player_girl.Player_Girl)
	Select obstacle_kitty\kitty_type$
		Case "sleeping_kitty"
			obstacle_kitty\step_index = obstacle_kitty\step_index + 1
			If obstacle_kitty\step_index >= 90 Then
				obstacle_kitty\step_index = 0
			EndIf
			obstacle_kitty\image = sleeping_kitty_frames(Floor(obstacle_kitty\step_index / 5))
		Case "singing_kitty"
			obstacle_kitty\step_index = obstacle_kitty\step_index + 1
			If obstacle_kitty\step_index >= 15 Then
				obstacle_kitty\step_index = 0
			EndIf
			obstacle_kitty\image = singing_kitty_frames(Floor(obstacle_kitty\step_index / 5))
		Case "flying_kitty"
			obstacle_kitty\y = obstacle_kitty\y + Sin(obstacle_kitty\x) * 10.0
	End Select
	
	obstacle_kitty\x = obstacle_kitty\x - game_speed
	If obstacle_kitty\x <= -obstacle_kitty\image_width Then
		Delete obstacle_kitty
		If player_girl\snack_bullet_count = 0 Then
			player_girl\snack_bullet_count = 3
		EndIf
		kitty_object_count = kitty_object_count - 1
	EndIf
End Function
Function Obstacle_Kitty_Draw(obstacle_kitty.Obstacle_Kitty)
	If obstacle_kitty <> Null Then
		DrawImage obstacle_kitty\image,obstacle_kitty\x,obstacle_kitty\y
	EndIf
End Function


;###################ObstacleObjectDataAndMethodsEnd#############################################################


;###################HealthHeartObjectDataAndMethodsStart########################################################


Type Health_Heart_Pickup
	Field image
	Field x,y
	Field health_value
	Field speed
End Type
Function Init_Health_Heart_Pickup.Health_Heart_Pickup(heart_img,x,y,speed,health_value)
	health_heart.Health_Heart_Pickup = New Health_Heart_Pickup
	health_heart\image = heart_img
	health_heart\x = x
	health_heart\y = y
	health_heart\health_value = health_value
	health_heart\speed = speed
	
	Return health_heart_object.Health_Heart_Pickup
End Function
Function Update_Health_Heart(health_heart_object.Health_Heart_Pickup)
	health_heart_object\x = health_heart_object\x - health_heart_object\speed
	If health_heart_object\x < -ImageWidth(health_heart_object\image) Then
		Delete health_heart_object
	EndIf
End Function
Function Draw_Health_Heart(health_heart_object.Health_Heart_Pickup)
	If health_heart_object <> Null Then
		DrawImage health_heart_object\image,health_heart_object\x,health_heart_object\y
	EndIf 
End Function

;###################HealthHeartObjectDataAndMethodsEnd############################################################



;###################MainloopAndHelperFunctions########################################################
;Dim white_col(2)
;white_col(0) = 255 : white_col(1) = 255 : white_col(2) = 255
;Dim blitz_blue_col(2)
;blitz_blue_col(0) = 0 : blitz_blue_col(1) = 42 : blitz_blue_col(2) = 64
;ClsColor white_col(0),white_col(1),white_col(2)
;ClsColor blitz_blue_col(0),blitz_blue_col(1),blitz_blue_col(2)
Const ESC_KEY = 1
run = True
While run
	If KeyHit(ESC_KEY) Then
		run = False
	EndIf
	Cls 
	
	
	Select game_state
			
		Case 0
			Game_Menu(title_font,normal_font,game_over_screen_image,title_screen_image)
		Case 1
			Update_Draw_BG_Floor(bg_floor_image,bg_wall_image,bg_topwall_image)
			
			Kitchen_Obj_Spawner()
			For kitchen_obj.Kitchen_Object = Each Kitchen_Object
				Kitchen_Obj_Update(kitchen_obj)
				Kitchen_Obj_Draw(kitchen_obj)
			Next
			
			Obstacle_Kitty_Spawner()
			For obstacle_kitty.Obstacle_Kitty = Each Obstacle_Kitty
				Obstacle_Kitty_Update(obstacle_kitty,player_girl)
				Obstacle_Kitty_Draw(obstacle_kitty)
				
				If obstacle_kitty <> Null Then
					If ImagesCollide (player_girl\image,player_girl\x,player_girl\y,0,obstacle_kitty\image,obstacle_kitty\x,obstacle_kitty\y,0) And (player_girl\bool_hit_flag = False) Then
						player_girl\bool_hit_flag = True
						player_girl\health_points = player_girl\health_points - 1
						player_girl\last_time_hit = MilliSecs()
						
					EndIf
				EndIf
			Next
			
			For snack_bullet_object.Snack_Bullet = Each Snack_Bullet
				Update_Snack_Bullet(snack_bullet_object)
				Draw_Snack_Bullet(snack_bullet_object)
				If snack_bullet_object <> Null Then
					For obstacle_kitty.Obstacle_Kitty = Each Obstacle_Kitty
						If ImagesCollide(snack_bullet_object\image,snack_bullet_object\x,snack_bullet_object\y,0,obstacle_kitty\image,obstacle_kitty\x,obstacle_kitty\y,0) Then
							Delete snack_bullet_object
							If player_girl\health_points < 3 Then
								health_heart_object.Health_Heart_Pickup = Init_Health_Heart_Pickup(health_heart_pickup_img,obstacle_kitty\x+ImageWidth(obstacle_kitty\image)/2,obstacle_kitty\y+ImageHeight(obstacle_kitty\image)/2,game_speed,1)
							EndIf
							Delete obstacle_kitty
							score = score + 500
							kitties_pacified = kitties_pacified + 1
							kitty_object_count = kitty_object_count - 1
							
							Exit
						EndIf
					Next
				EndIf
			Next
			
			For health_heart_object.Health_Heart_Pickup = Each Health_Heart_Pickup
				Update_Health_Heart(health_heart_object)
				Draw_Health_Heart(health_heart_object)
				
				If health_heart_object <> Null Then
					If ImagesOverlap(health_heart_object\image,health_heart_object\x,health_heart_object\y,player_girl\image,player_girl\x,player_girl\y) Then
						If player_girl\health_points < 3 Then
							player_girl\health_points = player_girl\health_points + health_heart_object\health_value
							Delete health_heart_object
							Exit
						EndIf
					EndIf
				EndIf
			Next
			
			
			Player_Girl_Draw(player_girl)
			Player_Girl_Update(player_girl)
			Update_Player_Score(normal_font)
			Show_Snack_Bullet_Count(normal_font,player_girl)
			Show_Kitty_Pacified_Count(normal_font)
			
		Case 2
			Win_Screen(win_screen_image,normal_font)
	End Select
	
	WaitTimer(fps_timer)
	Flip
Wend
FlushKeys
WaitKey
End

Function Create_Kitchen_Wall_IMG(col_r,col_g,col_b)
	img_surf = CreateImage(SCREEN_WIDTH,300)
	SetBuffer ImageBuffer(img_surf)
	Color col_r,col_g,col_b
	Rect 0,0,SCREEN_WIDTH,300,True
	SetBuffer BackBuffer()
	Return img_surf
End Function

Function Update_Draw_BG_Floor(floor_image,bg_wall_image,bg_topwall_image)
	img_width = ImageWidth(floor_image)
	img_height = ImageHeight(floor_image)
	bg_wall_img_height = ImageHeight(bg_wall_image)
	DrawImage bg_topwall_image,0,0
	DrawImage bg_wall_image,0,(SCREEN_HEIGHT/2 - bg_wall_img_height/2)
	DrawImage floor_image,bg_x_pos,SCREEN_HEIGHT-img_height
	DrawImage floor_image,img_width+bg_x_pos,SCREEN_HEIGHT-img_height
	bg_x_pos = bg_x_pos - game_speed/4
	If bg_x_pos <= -img_width Then
		bg_x_pos = 0
	EndIf
End Function

Function Game_Menu(title_font,normal_font,game_over_image,title_image)
	FlushKeys
	
	If score > hi_score Then
		file_out = WriteFile(save_score_dir$+"hi_score.txt")
		hi_score = score
		WriteInt file_out,hi_score
		CloseFile file_out
	EndIf
	
	
	Color 200,200,200
	Rect 0,0,SCREEN_WIDTH,SCREEN_HEIGHT,True
	Draw_Text("Kitty Dash'n Dodge",SCREEN_WIDTH/2,SCREEN_HEIGHT/3 - 120,title_font,0,0,0,True,True)
	
	
	
	If ko_count > 0 Then
		DrawImage game_over_image,SCREEN_WIDTH/2,SCREEN_HEIGHT/2+50
		x_offset = 20
		y_offset = -100
		Draw_Text("N-No!",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,True,True)
		x_offset = x_offset + 20
		y_offset = y_offset + 60
		Draw_Text("They've got me!",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,True,True)
		
		score_width = StringWidth(Str(score))
		hi_score_width = StringWidth(Str(hi_score))
		ko_width = StringWidth(Str(ko_count))
		Draw_Text ("Times KO'ed by kitties: "+Str(ko_count),SCREEN_WIDTH-500-ko_width,SCREEN_HEIGHT-120,normal_font,0,0,0,False,False)
		Draw_Text("Current Score: "+Str(score),SCREEN_WIDTH-500-score_width,SCREEN_HEIGHT-90,normal_font,0,0,0,False,False)
		Draw_Text("Hi-Score: "+Str(hi_score),SCREEN_WIDTH-500-hi_score_width,SCREEN_HEIGHT-60,normal_font,0,0,0,False,False)
		
		win_count_width = StringWidth(Str(win_count))
		Draw_Text("Win Count: "+Str(win_count),SCREEN_WIDTH-500-win_count_width,SCREEN_HEIGHT-150,normal_font,0,0,0,False,False)
		
		
		
		
		Draw_Text("Press Any Key to Restart!",SCREEN_WIDTH-500,SCREEN_HEIGHT-30,normal_font,0,0,0,False,False)
	ElseIf ko_count = 0 Then
		DrawImage title_image,SCREEN_WIDTH/2,SCREEN_HEIGHT/2+50
		x_offset = 90
		y_offset = -130
		Draw_Text("I forgot to buy",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		y_offset = y_offset + 30
		x_offset = x_offset + 20
		Draw_Text("new cat food.",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset -55
		y_offset = y_offset + 30
		Draw_Text("Now my kitties are",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset + 20
		y_offset = y_offset + 30
		Draw_Text("quite mad at me.",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset + 20
		y_offset = y_offset + 40
		Draw_Text("Let's grab some",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset - 40
		y_offset = y_offset + 30
		Draw_Text("kitty snackies and",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset + 20
		y_offset = y_offset + 30
		Draw_Text("break thru their",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		y_offset = y_offset + 30
		Draw_Text("blockade. We've",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		y_offset = y_offset + 30
		Draw_Text("gotta make it to",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset + 20
		y_offset = y_offset + 30
		Draw_Text("the grocery's",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		x_offset = x_offset + 75
		y_offset = y_offset + 30
		Draw_Text("alive!",SCREEN_WIDTH/2+x_offset,SCREEN_HEIGHT/2+y_offset,normal_font,0,0,0,False,False)
		
		
		
		score_width = StringWidth(Str(score))
		hi_score_width = StringWidth(Str(hi_score))
		
		Draw_Text("Current Score: "+Str(score),SCREEN_WIDTH-500-score_width,SCREEN_HEIGHT-90,normal_font,0,0,0,False,False)
		Draw_Text("Hi-Score: "+Str(hi_score),SCREEN_WIDTH-500-hi_score_width,SCREEN_HEIGHT-60,normal_font,0,0,0,False,False)
		Draw_Text("Press Any Key to Start!",SCREEN_WIDTH-500,SCREEN_HEIGHT-30,normal_font,0,0,0,False,False)
	EndIf
	
	Flip
	WaitKey
	If KeyHit(ESC_KEY) Then End
	FlushKeys
	Reset_Game()
End Function

Function Win_Screen(win_screen_image,normal_font)
	FlushKeys
	Color 10,10,10
	Rect 0,0,SCREEN_WIDTH,SCREEN_HEIGHT,True
	DrawImage win_screen_image,0,0
	Draw_Text("Current Score: "+Str(score),SCREEN_WIDTH/2+100,SCREEN_HEIGHT/2-120,normal_font,255,255,255,False,False)
	Draw_Text("Hi-Score: "+Str(hi_score),SCREEN_WIDTH/2+100,SCREEN_HEIGHT/2-90,normal_font,255,255,255,False,False)
	Draw_Text("Win Count: "+Str(win_count),SCREEN_WIDTH-500,SCREEN_HEIGHT/2-60,normal_font,255,255,255,False,False)
	Draw_Text("Press Any Key to Start!",SCREEN_WIDTH-500,SCREEN_HEIGHT/2-30,normal_font,255,255,255,False,False)
	Flip
	Delay 2000
	WaitKey
	If KeyHit(ESC_KEY) Then End
	game_state = 1
	FlushKeys
End Function

Function Draw_Text(text_string$,x,y,font,font_col_r,font_col_g,font_col_b,b_center_x,b_center_y)
	SetFont font
	Color font_col_r,font_col_g,font_col_b
	Text x,y,text_string$,b_center_x,b_center_y
End Function

Function Update_Player_Score(score_font)
	score = score + 1
	If score Mod 1000 = 0 Then
		game_speed = game_speed + 1
	EndIf
	If score Mod 10000 = 0 Then
		win_count = win_count + 1
		game_state = 2
	EndIf
	score_string$ = Str(score)
	hiscore_string$ = Str(hi_score)
	Draw_Text("Score: "+score_string$,SCREEN_WIDTH-100-StringWidth(score_string$),20,score_font,255,255,255,True,True)
	Draw_Text("Hi-Score: "+hiscore_string$,SCREEN_WIDTH-100-StringWidth(hiscore_string$),45,score_font,255,255,255,True,True)
End Function

Function Show_Snack_Bullet_Count(score_font,player_instance.Player_Girl)
	Draw_Text("Kitty Snacks: "+Str(player_instance\snack_bullet_count),SCREEN_WIDTH/2-100,20,score_font,255,255,255,True,True)
End Function
Function Show_Kitty_Pacified_Count(score_font)
	Draw_Text("Kitties Pacified: "+Str(kitties_pacified),SCREEN_WIDTH/2-100,45,score_font,255,255,255,True,True)
End Function 
Function Reset_Game()
	score = 0
	kitties_pacified = 0
	game_speed = 15
	game_state = 1
	
	For obstacle_kitty.Obstacle_Kitty = Each Obstacle_Kitty
		Delete obstacle_kitty
		kitty_object_count = kitty_object_count - 1
	Next
	
	For snack_bullet_object.Snack_Bullet = Each Snack_Bullet
		Delete snack_bullet_object
	Next
	
	For player_girl.Player_Girl = Each Player_Girl
		player_girl\x = player_girl\init_x_pos
		player_girl\y = player_girl\init_y_pos
		player_girl\step_index = 0
		player_girl\image = player_run_frames(player_girl\step_index)
		player_girl\bool_run = True
		player_girl\bool_jump = False
		player_girl\bool_slide = False
		player_girl\bool_draw_flag = True
		player_girl\health_points = 3
		player_girl\hit_flicker_sine# = 0
		player_girl\snack_bullet_count = 3
		player_girl\bool_hit_flag = False
	Next
	
End Function

;########################MainLoopAndHelperFunctionsEnd########################################
;~IDEal Editor Parameters:
;~C#Blitz3D