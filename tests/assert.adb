with Ada.Text_IO; use Ada.Text_IO;

procedure Test is
	x : Integer;
	y : Integer;
	b : Boolean;
	procedure assert ( b : boolean ) is begin
		if b then put('.'); else put('!'); end if;
	end assert;
	procedure digit ( x : Integer ) is begin
		put(character'val(48+x));
	end digit;
	type R is record 
		x : Integer; 
		y : Integer; 
	end record;
	u : R;
	v : R;
begin
	assert(256 < 257);
	assert(128 >= 64);
	assert(15 - 5 <= 10 and 15 - 5 > 9);
	assert(true);
	assert(not false);
	assert(true or false);
	assert(not (false and true));
	assert(5 + 12 = 17);
	assert(12 - 1 /= 12);
	u.x := 5;
	u.y := 2;
	v.x := 2 + 3;
	v.y := 5 - 3;
	assert(u = v); assert(not u /= v);
	v.y := 1;
	assert(u /= v); assert(not u = v);
	v := u;
	assert(v = u); assert(not u /= v);
	v.x := u.y;
	assert(v /= u); assert(not u = v);
	assert(12 / 2 = 6);
	assert(19 / 4 = 4);
	assert( 215 rem 3 = 2 );
	x := 0;
	for i in 1..15 loop x := x + 1; end loop;
	assert(x = 15);
	x := 0;
	for i in 15..1 loop x := x + 1; put('#'); end loop;
	assert(x = 0);
	x := 0;
	y := 16;
	b := true;
	for i in reverse 1..15 loop 
		x := x + 1; 
		b := b and y > i; 
		y := i; 
	end loop;
	assert(x = 15);
	assert(b);
end Test;