;##################GeneralGameWindowSetupStart############################################################################
AppTitle "Runnergirl Kitty Dodge"
Const SCREEN_WIDTH = 1100 : Const SCREEN_HEIGHT = 600
Graphics SCREEN_WIDTH,SCREEN_HEIGHT,0,2
Const FPS_CAP = 30
fps_timer = CreateTimer(FPS_CAP)
SetBuffer BackBuffer()
SeedRnd MilliSecs()

Const ASSET_PATH$ = "Assets/"
Global game_speed = 15
Global bg_x_pos = 0


bg_floor_image = LoadImage(ASSET_PATH$+"BG_images/scrolling_floor.png")
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

player_hurt_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_hurt_01.png")
MaskImage player_hurt_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
player_jump_img = LoadImage(ASSET_PATH$+"Player_Images/Gothgirl_jump_01.png")
MaskImage player_jump_img,player_mask_color(0),player_mask_color(1),player_mask_color(2)
player_health_heart_img = LoadImage(ASSET_PATH$+"Gui_Elements/health_heart_01.png")
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
	
	Player_Girl_Show_Health_Hearts(player_instance)
	
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
Function Player_Girl_Show_Health_Hearts(player_instance.Player_Girl)
	heart_img_width = ImageWidth(player_instance\health_heart_image)
	For heart_number = 0 To player_instance\health_points-1
		DrawImage player_instance\health_heart_image,20 + (heart_number*heart_img_width) + (heart_number*10),10
	Next
End Function

player_girl.Player_Girl = Init_Player_Self(player_hurt_img,player_jump_img,player_health_heart_img)
;###################PlayerDataAndMethodsEnd####################################################################


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
			obstacle_kitty\y = 400
		Case "singing_kitty"
			obstacle_kitty\image = singing_kitty_frames(obstacle_kitty\step_index)
			obstacle_kitty\image_width = ImageWidth(obstacle_kitty\image)
			obstacle_kitty\y = 400
		Case "flying_kitty"
			obstacle_kitty\image = flying_obstacle_kitty_img
			obstacle_kitty\image_width = ImageWidth(obstacle_kitty\image)
			obstacle_kitty\y = 350
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
Function Obstacle_Kitty_Update(obstacle_kitty.Obstacle_Kitty)
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
		kitty_object_count = kitty_object_count - 1
	EndIf
End Function
Function Obstacle_Kitty_Draw(obstacle_kitty.Obstacle_Kitty)
	If obstacle_kitty <> Null Then
		DrawImage obstacle_kitty\image,obstacle_kitty\x,obstacle_kitty\y
	EndIf
End Function


;###################ObstacleObjectDataAndMethodsEnd#############################################################




;###################MainloopAndHelperFunctions########################################################
;Dim white_col(2)
;white_col(0) = 255 : white_col(1) = 255 : white_col(2) = 255
Dim blitz_blue_col(2)
blitz_blue_col(0) = 0 : blitz_blue_col(1) = 42 : blitz_blue_col(2) = 64
;ClsColor white_col(0),white_col(1),white_col(2)
ClsColor blitz_blue_col(0),blitz_blue_col(1),blitz_blue_col(2)
Const ESC_KEY = 1
run = True
While run
	If KeyHit(ESC_KEY) Then
		run = False
	EndIf
	Cls 
	WaitTimer(fps_timer)
	Update_Draw_BG_Floor(bg_floor_image,bg_wall_image,bg_topwall_image)
	
	Kitchen_Obj_Spawner()
	For kitchen_obj.Kitchen_Object = Each Kitchen_Object
		Kitchen_Obj_Update(kitchen_obj)
		Kitchen_Obj_Draw(kitchen_obj)
	Next
	
	Obstacle_Kitty_Spawner()
	For obstacle_kitty.Obstacle_Kitty = Each Obstacle_Kitty
		Obstacle_Kitty_Update(obstacle_kitty)
		Obstacle_Kitty_Draw(obstacle_kitty)
		
		If obstacle_kitty <> Null Then
			If ImagesCollide (player_girl\image,player_girl\x,player_girl\y,0,obstacle_kitty\image,obstacle_kitty\x,obstacle_kitty\y,0) And (player_girl\bool_hit_flag = False) Then
				player_girl\bool_hit_flag = True
				player_girl\health_points = player_girl\health_points - 1
				player_girl\last_time_hit = MilliSecs()
				
			EndIf
		EndIf
	Next
	
	Player_Girl_Update(player_girl)
	Player_Girl_Draw(player_girl)
	
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

;########################MainLoopAndHelperFunctionsEnd########################################
;~IDEal Editor Parameters:
;~C#Blitz3D