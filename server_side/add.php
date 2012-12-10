<?php

	$db =  new PDO('sqlite:db.sqlite');

$name=$_POST['name'];
$rh=$_POST['rh'];
$gh=$_POST['gh'];
$bh=$_POST['bh'];
$contour=$_POST['contour'];
$rgba=$_POST['rgba'];
$img=$_POST['img'];

	$query='INSERT INTO DESCRIPTORs VALUES(NULL,"'.$name.'","'.$rh.'","'.$gh.'","'.$bh.'","'.$contour.'","'.$rgba.'","'.$img.'");';
try {


  $sth=$db->prepare($query);
  $sth->execute();


} catch (Exception $e) {
	echo "error";

}

echo "ok";
?>
