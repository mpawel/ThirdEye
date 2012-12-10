<?php

$name=$_POST['name'];

$db = new PDO("sqlite:db.sqlite");


	$query="SELECT * FROM DESCRIPTORs WHERE name='".$name."';";

	$statement=$db->prepare($query);
	$statement->execute();
	$result=$statement->fetchAll(PDO::FETCH_ASSOC);
	$list = array("list" => $result);
	echo	json_encode($list);

?>
