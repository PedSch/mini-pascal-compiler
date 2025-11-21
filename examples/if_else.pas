program conditionals;
var x, y, max: integer;

begin
    x := 15;
    y := 20;
    
    if x > y then
        max := x
    else
        max := y;
    
    writeln(max)
end.
